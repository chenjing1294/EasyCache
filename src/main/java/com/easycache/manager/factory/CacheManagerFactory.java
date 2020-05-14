package com.easycache.manager.factory;

import com.easycache.manager.CacheManager;
import com.easycache.manager.CacheManagerInvocationHandler;

import java.lang.reflect.Proxy;

/**
 * 缓存管理器生产工厂
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public abstract class CacheManagerFactory {
    public final CacheManager create() {
        CacheManager cacheManager = createCacheManager();
        return createProxy(cacheManager);
    }

    private CacheManager createProxy(CacheManager cacheManager) {
        CacheManagerInvocationHandler handler = new CacheManagerInvocationHandler(cacheManager);
        return (CacheManager) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{CacheManager.class},
                handler);
    }

    protected abstract CacheManager createCacheManager();
}
