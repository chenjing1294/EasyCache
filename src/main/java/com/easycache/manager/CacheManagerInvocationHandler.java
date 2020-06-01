package com.easycache.manager;

import com.easycache.CacheKey;
import com.easycache.CacheValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对缓存管理器进行代理，降低多线程环境下对缓存层操作的压力
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class CacheManagerInvocationHandler implements InvocationHandler {
    private final CacheManager cacheManager;
    private final ConcurrentHashMap<CacheKey, Process> processing = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(CacheManagerInvocationHandler.class);

    public CacheManagerInvocationHandler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method get = CacheManager.class.getMethod("get", CacheKey.class, Type.class);
        Method set = CacheManager.class.getMethod("set", CacheKey.class, CacheValue.class);
        Method delete = CacheManager.class.getMethod("delete", CacheKey.class);
        Method batchDelete = CacheManager.class.getMethod("batchDelete", CacheKey.class);
        //短时间内大量线程查询同一个键时（第一个查询线程还没执行结束，又来了线程），降低下一次缓存层的压力
        if (method.equals(get)) {
            CacheKey cacheKey = (CacheKey) args[0];
            Process process = processing.get(cacheKey);
            if (process == null) {
                Process firstProcess = new Process();
                Process p = processing.putIfAbsent(cacheKey, firstProcess);
                if (p != null) {//后续的线程
                    logger.trace("后续线程1>:{}", Thread.currentThread().getName());
                    process = p;
                    synchronized (process) {
                        while (!process.isFirstFinished()) {
                            logger.trace("后续线程进入等待集1>:{}", Thread.currentThread().getName());
                            process.wait();//放弃process锁，进入该条件的等待集
                        }
                        logger.trace("后续线程被唤醒1>:{}", Thread.currentThread().getName());
                        return process.getCacheValue();
                    }
                } else {//第一个线程
                    logger.trace("第一个线程:{}", Thread.currentThread().getName());
                    process = firstProcess;
                    //Thread.sleep(2000);
                    synchronized (process) {
                        //Thread.sleep(2000);
                        try {
                            CacheValue res = (CacheValue) method.invoke(cacheManager, args);
                            logger.trace("第一个线程从缓存中得到结果:{}", res);
                            process.setFirstFinished(true);
                            process.setCacheValue(res);
                            return res;
                        } finally {
                            processing.remove(cacheKey);
                            logger.trace("第一个线程通知其他线程:{}", Thread.currentThread().getName());
                            process.notifyAll();//通知等待集中的线程
                        }
                    }
                }
            } else {//后续的线程
                logger.trace("后续线程2>:{}", Thread.currentThread().getName());
                synchronized (process) {
                    while (!process.isFirstFinished()) {
                        logger.trace("后续线程进入等待集2>:{}", Thread.currentThread().getName());
                        process.wait();//放弃process锁，进入该条件的等待集
                    }
                    logger.trace("后续线程被唤醒2>:{}", Thread.currentThread().getName());
                    return process.getCacheValue();
                }
            }
        }
        return method.invoke(cacheManager, args);
    }

    private class Process {
        private boolean firstFinished;
        private CacheValue cacheValue;

        public Process() {
            this.firstFinished = false;
        }

        public CacheValue getCacheValue() {
            return cacheValue;
        }

        public void setCacheValue(CacheValue cacheValue) {
            this.cacheValue = cacheValue;
        }

        public boolean isFirstFinished() {
            return firstFinished;
        }

        public void setFirstFinished(boolean firstFinished) {
            this.firstFinished = firstFinished;
        }
    }
}
