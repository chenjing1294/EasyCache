package com.easycache.manager.factory;

import com.easycache.EasyCacheConfig;
import com.easycache.manager.CacheManager;
import com.easycache.manager.impl.WeakHashMapCacheManager;

public class WeakHashMapCacheManagerFactory extends CacheManagerFactory {
    private EasyCacheConfig easyCacheConfig;
    private CacheManager nextCacheManager;

    public WeakHashMapCacheManagerFactory(EasyCacheConfig easyCacheConfig,
                                          CacheManager nextCacheManager) {
        this.easyCacheConfig = easyCacheConfig;
        this.nextCacheManager = nextCacheManager;
    }

    @Override
    protected CacheManager createCacheManager() {
        return new WeakHashMapCacheManager(easyCacheConfig, nextCacheManager);
    }
}
