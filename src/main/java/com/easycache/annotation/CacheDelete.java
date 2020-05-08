package com.easycache.annotation;

import java.lang.annotation.*;

/**
 * 删除缓存注解
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface CacheDelete {
    /**
     * 缓存的条件表达式，如果得到的值是 false 的话，不会将缓存应用到方法调用上
     */
    String condition() default "true";

    /**
     * 要删除的缓存的键（支持通配符：* ? []）
     */
    String[] keys();

    /**
     * 设置哈希表中的字段，如果设置此项，则删除哈希表中指定的项，支持SpringEL表达式
     */
    String[] hkeys() default "";

    /**
     * 是否在方法执行前就清空，缺省为 false，如果指定为 true，
     * 则在方法还没有执行的时候就清空缓存，缺省情况下，如果方法执行抛出异常，则不会清空缓存
     */
    boolean beforeInvocation() default false;
}
