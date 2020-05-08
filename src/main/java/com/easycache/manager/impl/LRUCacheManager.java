package com.easycache.manager.impl;

import com.easycache.CacheKey;
import com.easycache.CacheValue;
import com.easycache.EasyCacheConfig;
import com.easycache.manager.CacheManager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于LRU算法的缓存管理
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class LRUCacheManager extends AbstractMemoryCacheManager {
    private final Map<CacheKey, CacheValue> store;
    private int MAX_CACHE_SIZE;

    public LRUCacheManager(EasyCacheConfig easyCacheConfig, int maxCacheSize) {
        super(easyCacheConfig);
        this.MAX_CACHE_SIZE = maxCacheSize > 0 && maxCacheSize < 500 ? maxCacheSize : 100;
        this.store = Collections.synchronizedMap(new LRUMap<>(MAX_CACHE_SIZE));
    }

    public LRUCacheManager(EasyCacheConfig easyCacheConfig, CacheManager cacheManager, int maxCacheSize) {
        super(easyCacheConfig, cacheManager);
        this.MAX_CACHE_SIZE = maxCacheSize > 0 && maxCacheSize < 500 ? maxCacheSize : 100;
        this.store = Collections.synchronizedMap(new LRUMap<>(MAX_CACHE_SIZE));
    }

    @Override
    protected Map<CacheKey, CacheValue> getStore() {
        return store;
    }

    private final class LRUMap<K, V> extends LinkedHashMap<K, V> {

        private final int max;

        public LRUMap(int max) {
            super((int) (max * 1.4f), 0.75f, true);
            this.max = max;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > max;
        }
    }
}
