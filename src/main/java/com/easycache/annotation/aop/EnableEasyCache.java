package com.easycache.annotation.aop;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 在Spring项目中快速使用EasyCache
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({DefaultEasyCacheConfig.class, DefaultAOPConfig.class})
public @interface EnableEasyCache {
}
