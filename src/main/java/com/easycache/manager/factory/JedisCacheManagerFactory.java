package com.easycache.manager.factory;

import com.easycache.EasyCacheConfig;
import com.easycache.manager.CacheManager;
import com.easycache.manager.impl.JedisCacheManager;
import com.easycache.serializer.Serializer;
import redis.clients.jedis.JedisPool;

public class JedisCacheManagerFactory extends CacheManagerFactory {
    private JedisPool jedisPool;
    private Serializer<String> keySerializer;
    private Serializer<Object> valueSerializer;
    private EasyCacheConfig easyCacheConfig;
    private CacheManager nextCacheManager;

    public JedisCacheManagerFactory(JedisPool jedisPool,
                                    Serializer<String> keySerializer,
                                    Serializer<Object> valueSerializer,
                                    EasyCacheConfig easyCacheConfig,
                                    CacheManager nextCacheManager) {
        this.jedisPool = jedisPool;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.easyCacheConfig = easyCacheConfig;
        this.nextCacheManager = nextCacheManager;
    }

    @Override
    protected CacheManager createCacheManager() {
        return new JedisCacheManager(
                jedisPool,
                keySerializer,
                valueSerializer,
                easyCacheConfig,
                nextCacheManager);

    }
}
