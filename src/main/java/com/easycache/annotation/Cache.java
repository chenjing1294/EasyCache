package com.easycache.annotation;

import com.easycache.CacheOperateType;

import java.lang.annotation.*;

/**
 * 缓存注解
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface Cache {
    /**
     * 缓存的过期时间，默认永不过期
     */
    int expire() default -1;

    /**
     * 自定义的缓存key，支持SpringEL表达式
     */
    String key();

    /**
     * 设置哈希表中的字段，如果设置此项，则用哈希表进行存储，支持SpringEL表达式
     */
    String hkey() default "";

    /**
     * 缓存的条件表达式，如果得到的值是 false 的话，不会将缓存应用到方法调用上
     */
    String condition() default "true";

    /**
     * 缓存的操作类型默认是READ_WRITE
     */
    CacheOperateType cacheOperateType() default CacheOperateType.READ_WRITE;

    /**
     * 该键所属的命名空间，如果设置，则EasyCacheConfig中配置的命名空间失效
     */
    String namespace() default "";
}
