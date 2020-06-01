package com.easycache;

/**
 * EasyCacheConfig框架的全局配置
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class EasyCacheConfig {
    private String namespace;
    private RedisConfig redisConfig;

    public EasyCacheConfig() {
        this.namespace = "EasyCache";
        this.redisConfig = new RedisConfig();
    }

    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    public void setRedisConfig(RedisConfig redisConfig) {
        this.redisConfig = redisConfig;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public class RedisConfig {
        private Integer database;
        private String host;
        private String password;
        private Integer port;

        public RedisConfig() {
            this.host = "localhost";
            this.port = 6379;
            this.database = 0;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getDatabase() {
            return database;
        }

        public void setDatabase(Integer database) {
            this.database = database;
        }
    }
}
