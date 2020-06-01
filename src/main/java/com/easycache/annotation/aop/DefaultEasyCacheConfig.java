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
import com.easycache.util.C;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
    public JedisPool defaultJedisPool(EasyCacheConfig easyCacheConfig) {
        String host = easyCacheConfig.getRedisConfig().getHost();
        Integer port = easyCacheConfig.getRedisConfig().getPort();
        String password = easyCacheConfig.getRedisConfig().getPassword();
        Integer database = easyCacheConfig.getRedisConfig().getDatabase();
        return new JedisPool(new GenericObjectPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, password, database);
    }

    @Bean
    @Qualifier("defaultEasyCacheConfig")
    public EasyCacheConfig defaultCacheConfig() throws IOException {
        com.easycache.EasyCacheConfig easyCacheConfig = new EasyCacheConfig();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/easyCache.properties");
        if (resourceAsStream != null) {
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            String namespace = properties.getProperty("easyCache.namespace");
            if (namespace != null) {
                easyCacheConfig.setNamespace(C.getValue(namespace));
            }

            String host = properties.getProperty("easyCache.redis.host");
            if (host != null) {
                easyCacheConfig.getRedisConfig().setHost(C.getValue(host));
            }

            String port = properties.getProperty("easyCache.redis.port");
            if (port != null) {
                easyCacheConfig.getRedisConfig().setPort(Integer.valueOf(C.getValue(port)));
            }

            String database = properties.getProperty("easyCache.redis.database");
            if (database != null) {
                easyCacheConfig.getRedisConfig().setDatabase(Integer.valueOf(C.getValue(database)));
            }

            String password = properties.getProperty("easyCache.redis.password");
            if (password != null) {
                easyCacheConfig.getRedisConfig().setPassword(C.getValue(password));
            }
        }
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
