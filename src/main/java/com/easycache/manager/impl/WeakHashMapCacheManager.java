package com.easycache.manager.impl;

import com.easycache.CacheKey;
import com.easycache.CacheValue;
import com.easycache.EasyCacheConfig;
import com.easycache.manager.CacheManager;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * WeakHashMapCacheManager所管理的键被包装为WeakReference类型。缓存的键值对
 * 将在其键不再被正常使用时自动删除，也就是说如果没有指向该键的引用，那么当垃圾收集（
 * GC）进程执行时，其条目将从映射中有效删除。
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class WeakHashMapCacheManager extends AbstractMemoryCacheManager {
    private final Map<CacheKey, CacheValue> store;

    public WeakHashMapCacheManager(EasyCacheConfig easyCacheConfig) {
        super(easyCacheConfig);
        this.store = Collections.synchronizedMap(new WeakHashMap<>());
    }

    public WeakHashMapCacheManager(EasyCacheConfig easyCacheConfig, CacheManager cacheManager) {
        super(easyCacheConfig, cacheManager);
        this.store = Collections.synchronizedMap(new WeakHashMap<>());
    }

    @Override
    protected Map<CacheKey, CacheValue> getStore() {
        return store;
    }
}
