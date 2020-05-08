package com.easycache.manager.impl;

import com.easycache.CacheKey;
import com.easycache.CacheValue;
import com.easycache.EasyCacheConfig;
import com.easycache.manager.CacheManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractMemoryCacheManager implements CacheManager {
    private final EasyCacheConfig easyCacheConfig;
    private final CacheManager cacheManager;

    public AbstractMemoryCacheManager(EasyCacheConfig easyCacheConfig) {
        this.easyCacheConfig = easyCacheConfig;
        this.cacheManager = null;
    }

    public AbstractMemoryCacheManager(EasyCacheConfig easyCacheConfig, CacheManager cacheManager) {
        this.easyCacheConfig = easyCacheConfig;
        this.cacheManager = cacheManager;
    }

    @Override
    public <V> void set(CacheKey key, CacheValue<V> value) throws IOException {
        getStore().put(key, value);
        if (getNextCacheManager() != null) {
            getNextCacheManager().set(key, value);
        }
    }

    @Override
    public <V> CacheValue<V> get(CacheKey key, Type returnType) throws IOException {
        CacheValue<V> res = null;
        res = getStore().get(key);
        if (res != null && res.isExpired()) {
            delete(key);
            return null;
        }

        if (res == null && getNextCacheManager() != null) {
            res = getNextCacheManager().get(key, returnType);
        }

        //如果是从数据源获取的数据，则更新最后加载时间戳
        if (res != null && getNextCacheManager() == null) {
            res.setLastLoadTime(System.currentTimeMillis());
        }
        return res;
    }

    @Override
    public void delete(CacheKey key) throws IOException {
        try {
            getStore().remove(key);
        } finally {
            if (getNextCacheManager() != null) {
                getNextCacheManager().delete(key);
            }
        }
    }

    @Override
    public void batchDelete(CacheKey pattern) throws IOException {
        try {
            String p = pattern.getKey();
            Pattern compile = Pattern.compile(p);
            List<CacheKey> dels = new ArrayList<>();
            getStore().forEach((k, v) -> {
                String key = k.getKey();
                Matcher matcher = compile.matcher(key);
                if (matcher.matches()) {
                    dels.add(k);
                }
            });
            dels.forEach(getStore()::remove);
        } finally {
            if (getNextCacheManager() != null) {
                getNextCacheManager().batchDelete(pattern);
            }
        }
    }

    @Override
    public EasyCacheConfig getEasyCacheConfig() {
        return easyCacheConfig;
    }

    @Override
    public CacheManager getNextCacheManager() {
        return cacheManager;
    }

    protected abstract Map<CacheKey, CacheValue> getStore();
}
