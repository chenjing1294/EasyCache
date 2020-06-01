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
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

/**
 * EasyCache的默认配置
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
@Configuration
@EnableAspectJAutoProxy
public class DefaultEasyCacheConfig {
    private final Logger logger = LoggerFactory.getLogger(DefaultAOPConfig.class);

    @Bean
    @Qualifier("defaultJedisPool")
    public JedisPool defaultJedisPool(@Value("${spring.redis.host:localhost}") String host,
                                      @Value("${spring.redis.port:6379}") int port,
                                      @Value("${spring.redis.password:#{null}}") String password) {
        return new JedisPool(new GenericObjectPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, password);
    }

    @Bean
    @Qualifier("defaultEasyCacheConfig")
    public EasyCacheConfig defaultCacheConfig() {
        com.easycache.EasyCacheConfig easyCacheConfig = new EasyCacheConfig();
        easyCacheConfig.setNamespace("EasyCache");
        return easyCacheConfig;
    }

    @Bean
    @Qualifier("defaultEasyCacheManager")
    public CacheManager defaultEasyCacheManager(EasyCacheConfig easyCacheConfig, JedisPool jedisPool) {
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
