package com.easycache.annotation.aop;

import com.easycache.CacheKey;
import com.easycache.CacheOperateType;
import com.easycache.CacheValue;
import com.easycache.annotation.Cache;
import com.easycache.annotation.CacheDelete;
import com.easycache.annotation.script.SpringELParser;
import com.easycache.manager.CacheManager;
import com.easycache.util.A;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 切面处理方法
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class AspectjAopInterceptor {
    private final CacheManager cacheManager;
    private final SpringELParser springELParser;
    private final CacheHandler cacheHandler;
    private final Logger logger = LoggerFactory.getLogger(AspectjAopInterceptor.class);

    public AspectjAopInterceptor(CacheManager cacheManager, SpringELParser springELParser) {
        this.cacheManager = cacheManager;
        this.springELParser = springELParser;
        this.cacheHandler = new CacheHandler();
    }

    /**
     * 对用{@link Cache}进行注解的方法进行拦截处理，通知类型为环绕通知
     */
    public Object proceed(ProceedingJoinPoint pjp, Cache cache) throws Throwable {
        Object[] args = pjp.getArgs();
        Object target = pjp.getTarget();
        String condition = cache.condition();

        Boolean apply = springELParser.getELValue(condition, target, args, new Object(), Boolean.class);
        if (!apply) {
            return pjp.proceed(args);
        }
        CacheOperateType cacheOperateType = cache.cacheOperateType();
        if (cacheOperateType == CacheOperateType.WRITE) {//键的表达式里允许使用方法的返回值
            return cacheHandler.write(pjp, cache);
        }
        if (cacheOperateType == CacheOperateType.LOAD) {//忽略键的值
            return cacheHandler.load(pjp, cache);
        }
        if (cacheOperateType == CacheOperateType.READ) {//键的表达式里不允许使用方法的返回值
            return cacheHandler.read(pjp, cache);
        }
        if (cacheOperateType == CacheOperateType.READ_WRITE) {//键的表达式里不允许使用方法的返回值
            return cacheHandler.read_write(pjp, cache);
        }
        return null;
    }

    /**
     * 对用{@link CacheDelete}进行注解的方法进行拦截处理，通知类型为环绕通知
     */
    public Object cacheDelete(ProceedingJoinPoint pjp, CacheDelete cacheDelete) throws IOException {
        Object[] args = pjp.getArgs();
        Object target = pjp.getTarget();
        String[] keys = cacheDelete.keys();
        String[] hkeys = cacheDelete.hkeys();
        String condition = cacheDelete.condition();
        boolean beforeInvocation = cacheDelete.beforeInvocation();
        Throwable e = null;
        Object retVal = null;
        try {
            retVal = pjp.proceed(args);
        } catch (Throwable t) {
            e = t;
            t.printStackTrace();
        } finally {
            String namespace = cacheDelete.namespace();
            if (StringUtils.isEmpty(namespace)) {
                namespace = cacheManager.getEasyCacheConfig().getNamespace();
            }
            if (StringUtils.isEmpty(namespace)) {
                namespace = null;
            }
            if ((e == null) || (e != null && beforeInvocation)) {
                boolean _condition = springELParser.getELValue(condition, target, args, retVal, Boolean.class);
                if (_condition) {
                    for (String k : keys) {
                        String _k = springELParser.getELValue(k, target, args, retVal, String.class);
                        if (hkeys.length > 0) {
                            for (String hk : hkeys) {
                                String _hk = null;
                                if (hk.length() > 0) {
                                    _hk = springELParser.getELValue(hk, target, args, retVal, String.class);
                                }
                                CacheKey cacheKey = new CacheKey(namespace, _k, _hk);
                                if (A.isBatchDeleteKey(cacheKey)) {
                                    cacheManager.batchDelete(cacheKey);
                                } else {
                                    cacheManager.delete(cacheKey);
                                }
                            }
                        } else {
                            CacheKey cacheKey = new CacheKey(namespace, _k);
                            if (A.isBatchDeleteKey(cacheKey)) {
                                cacheManager.batchDelete(cacheKey);
                            } else {
                                cacheManager.delete(cacheKey);
                            }
                        }
                    }
                }
            }
            return retVal;
        }
    }


    protected class CacheHandler {
        protected Object write(ProceedingJoinPoint pjp, Cache cache) throws Throwable {
            Object[] args = pjp.getArgs();
            Object target = pjp.getTarget();
            Object res = pjp.proceed(args);
            CacheKey cacheKey = null;
            int expire = cache.expire();
            String key = springELParser.getELValue(cache.key(), target, args, res, String.class);
            String namespace = cache.namespace();
            if (StringUtils.isEmpty(namespace)) {
                namespace = cacheManager.getEasyCacheConfig().getNamespace();
            }
            if (StringUtils.isEmpty(namespace)) {
                namespace = null;
            }
            if (cache.hkey().length() > 0) {
                String hkey = springELParser.getELValue(cache.hkey(), target, args, res, String.class);
                cacheKey = new CacheKey(namespace, key, hkey);
            } else {
                cacheKey = new CacheKey(namespace, key);
            }
            CacheValue cacheValue = new CacheValue(res, expire);
            cacheValue.setLastLoadTime(System.currentTimeMillis());
            cacheManager.set(cacheKey, cacheValue);
            return res;
        }

        protected Object load(ProceedingJoinPoint pjp, Cache cache) throws Throwable {
            Object[] args = pjp.getArgs();
            return pjp.proceed(args);
        }

        protected Object read(ProceedingJoinPoint pjp, Cache cache) throws IOException {
            Object[] args = pjp.getArgs();
            Object target = pjp.getTarget();
            Object res = new Object();
            CacheKey cacheKey = null;
            String key = springELParser.getELValue(cache.key(), target, args, res, String.class);
            String hkey = null;
            if (cache.hkey().length() > 0) {
                hkey = springELParser.getELValue(cache.hkey(), target, args, res, String.class);
            }
            String namespace = cache.namespace();
            if (StringUtils.isEmpty(namespace)) {
                namespace = cacheManager.getEasyCacheConfig().getNamespace();
            }
            if (StringUtils.isEmpty(namespace)) {
                namespace = null;
            }
            cacheKey = new CacheKey(namespace, key, hkey);

            Signature signature = pjp.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
            Type returnType = method.getGenericReturnType();
            ParameterizedTypeImpl make = ParameterizedTypeImpl.make(CacheValue.class, new Type[]{returnType}, null);
            CacheValue cacheValue = cacheManager.get(cacheKey, make);
            return cacheValue == null ? null : cacheValue.getData();
        }

        protected Object read_write(ProceedingJoinPoint pjp, Cache cache) throws Throwable {
            Object data = read(pjp, cache);
            if (data == null) {
                return write(pjp, cache);
            } else {
                return data;
            }
        }
    }
}
