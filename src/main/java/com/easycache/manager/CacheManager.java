package com.easycache.manager;

import com.easycache.CacheKey;
import com.easycache.CacheValue;
import com.easycache.EasyCacheConfig;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 缓存管理器，用于对缓存中的数据进行操作
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public interface CacheManager {
    /**
     * 向缓存中写入数据
     *
     * @param key   键
     * @param value 值
     */
    <V> void set(final CacheKey key, final CacheValue<V> value) throws IOException;

    /**
     * 从缓存中获取值
     *
     * @param key 键
     */
    <V> CacheValue<V> get(final CacheKey key, Type returnType) throws IOException;

    /**
     * 删除缓存的数据
     *
     * @param key 键
     */
    void delete(final CacheKey key) throws IOException;

    /**
     * 批量删除缓存中的键（允许模糊查询的有3个通配符，分别是 * ? []）
     *
     * @param pattern 匹配模式
     */
    void batchDelete(final CacheKey pattern) throws IOException;

    /**
     * 获取全局的EasyCache配置
     */
    EasyCacheConfig getEasyCacheConfig();


    /**
     * 获取缓存链中的下一个缓存管理器
     */
    CacheManager getNextCacheManager();
}
