package com.easycache.manager.impl;

import com.easycache.CacheKey;
import com.easycache.CacheValue;
import com.easycache.EasyCacheConfig;
import com.easycache.manager.CacheManager;
import com.easycache.serializer.Serializer;
import com.easycache.util.A;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于Redis的缓存管理器
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class JedisCacheManager implements CacheManager {
    private final JedisPool jedisPool;
    private final Serializer<String> keySerializer;
    private final Serializer<Object> valueSerializer;
    private final EasyCacheConfig easyCacheConfig;
    private final CacheManager cacheManager;

    public JedisCacheManager(JedisPool jedisPool,
                             Serializer<String> keySerializer,
                             Serializer<Object> valueSerializer,
                             EasyCacheConfig easyCacheConfig) {
        this(jedisPool, keySerializer, valueSerializer, easyCacheConfig, null);
    }

    public JedisCacheManager(JedisPool jedisPool,
                             Serializer<String> keySerializer,
                             Serializer<Object> valueSerializer,
                             EasyCacheConfig easyCacheConfig,
                             CacheManager cacheManager) {
        this.jedisPool = jedisPool;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.easyCacheConfig = easyCacheConfig;
        this.cacheManager = cacheManager;
    }

    @Override
    public <V> void set(CacheKey key, CacheValue<V> value) throws IOException {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] k = keySerializer.serialize(key.getKey());
            byte[] val = valueSerializer.serialize(value);
            if (key.getSubKey() != null) {
                byte[] sk = keySerializer.serialize(key.getSubKey());
                jedis.hset(k, sk, val);
                if (value.getExpire() != CacheValue.NEVER_EXPIRE) {
                    jedis.expire(k, value.getExpire());
                }
            } else {
                jedis.set(k, val);
                if (value.getExpire() != CacheValue.NEVER_EXPIRE) {
                    jedis.expire(k, value.getExpire());
                }
            }
        }
        if (getNextCacheManager() != null) {
            getNextCacheManager().set(key, value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> CacheValue<V> get(CacheKey key, Type returnType) throws IOException {
        CacheValue<V> res = null;
        try (Jedis jedis = jedisPool.getResource()) {
            if (key != null && key.getKey() != null) {
                byte[] k = keySerializer.serialize(key.getKey());
                byte[] sk = null;
                if (key.getSubKey() != null) {
                    sk = keySerializer.serialize(key.getSubKey());
                }
                byte[] val = null;
                if (sk == null) {
                    val = jedis.get(k);
                } else {
                    val = jedis.hget(k, sk);
                }
                res = (CacheValue<V>) valueSerializer.deserialize(val, returnType);
            }
        }
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
        try (Jedis jedis = jedisPool.getResource()) {
            if (key != null && key.getKey() != null) {
                byte[] k = keySerializer.serialize(key.getKey());
                byte[] sk = null;
                if (key.getSubKey() != null) {
                    sk = keySerializer.serialize(key.getSubKey());
                }
                if (sk == null) {
                    jedis.del(k);
                } else {
                    jedis.hdel(k, sk);
                }
            }
        } finally {
            if (getNextCacheManager() != null) {
                getNextCacheManager().delete(key);
            }
        }
    }

    @Override
    public void batchDelete(CacheKey pattern) throws IOException {
        try (Jedis jedis = jedisPool.getResource()) {
            if (pattern != null && pattern.getKey() != null) {
                byte[] k = keySerializer.serialize(pattern.getKey());
                byte[] sk = null;
                if (pattern.getSubKey() != null) {
                    sk = keySerializer.serialize(pattern.getSubKey());
                }
                if (sk == null) {
                    Set<byte[]> keys = jedis.keys(k);
                    byte[][] ks = keys.toArray(new byte[0][0]);
                    jedis.del(ks);
                } else {
                    Set<byte[]> keys = jedis.keys(k);
                    for (byte[] i : keys) {
                        Set<byte[]> hkeys = jedis.hkeys(i);
                        List<byte[]> hks = new ArrayList<>();
                        for (byte[] hk : hkeys) {
                            String _hk = new String(hk, keySerializer.getCharset());
                            String _sk = new String(sk, keySerializer.getCharset());
                            _sk = A.getRegex(_sk);
                            Pattern compile = Pattern.compile(_sk);
                            Matcher matcher = compile.matcher(_hk);
                            if (matcher.matches()) {
                                hks.add(hk);
                            }
                        }
                        jedis.hdel(i, hks.toArray(new byte[0][0]));
                    }
                }
            }
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
}
