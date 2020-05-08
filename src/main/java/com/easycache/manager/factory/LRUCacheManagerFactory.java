package com.easycache.manager.factory;

import com.easycache.EasyCacheConfig;
import com.easycache.manager.CacheManager;
import com.easycache.manager.impl.LRUCacheManager;

public class LRUCacheManagerFactory extends CacheManagerFactory {
    private EasyCacheConfig easyCacheConfig;
    private CacheManager nextCacheManager;

    public LRUCacheManagerFactory(EasyCacheConfig easyCacheConfig,
                                  CacheManager nextCacheManager) {
        this.easyCacheConfig = easyCacheConfig;
        this.nextCacheManager = nextCacheManager;
    }

    @Override
    protected CacheManager createCacheManager() {
        return new LRUCacheManager(easyCacheConfig, nextCacheManager, -1);
    }
}
