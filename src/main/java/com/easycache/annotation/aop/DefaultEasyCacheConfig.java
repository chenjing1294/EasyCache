package com.easycache.annotation.aop;

import com.easycache.EasyCacheConfig;
import com.easycache.annotation.script.SpringELParser;
import com.easycache.manager.CacheManager;
import com.easycache.manager.factory.CacheManagerFactory;
import com.easycache.manager.factory.JedisCacheManagerFactory;
import com.easycache.manager.factory.LRUCacheManagerFactory;
import com.easycache.serializer.compressor.impl.CommonCompressor;
import com.easycache.serializer.impl.JacksonJsonSerializer;
import com.easycache.serializer.impl.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import redis.clients.jedis.JedisPool;

/**
 * EasyCache的默认配置
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
@Configuration
@EnableAspectJAutoProxy
public class DefaultEasyCacheConfig {
    @Bean
    @Qualifier("default")
    public JedisPool defaultJedisPool(@Value("${spring.redis.host:localhost}") String host,
                                      @Value("${spring.redis.port:6379}") int port) {
        return new JedisPool(host, port);
    }

    @Bean
    @Qualifier("default")
    public com.easycache.EasyCacheConfig defaultCacheConfig() {
        com.easycache.EasyCacheConfig easyCacheConfig = new com.easycache.EasyCacheConfig();
        easyCacheConfig.setNamespace("EasyCache");
        return easyCacheConfig;
    }

    @Bean
    public CacheManager easyCacheManager(EasyCacheConfig easyCacheConfig, JedisPool jedisPool) {
        CacheManagerFactory f1 = new JedisCacheManagerFactory(
                jedisPool,
                new StringSerializer(),
                new JacksonJsonSerializer(new CommonCompressor()),
                easyCacheConfig,
                null);
        CacheManager jedisCacheManager = f1.create();

        CacheManagerFactory f2 = new LRUCacheManagerFactory(easyCacheConfig, jedisCacheManager);
        return f2.create();
    }

    @Bean
    public AspectjAopInterceptor aspectjAopInterceptor(CacheManager cacheManager) {
        return new AspectjAopInterceptor(cacheManager, new SpringELParser());
    }
}
