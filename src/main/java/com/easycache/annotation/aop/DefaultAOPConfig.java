package com.easycache.annotation.aop;

import com.easycache.annotation.Cache;
import com.easycache.annotation.CacheDelete;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 默认的切面配置
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
@Aspect
@Component
public class DefaultAOPConfig {
    private final AspectjAopInterceptor aspectjAopInterceptor;

    @Autowired
    public DefaultAOPConfig(AspectjAopInterceptor aspectjAopInterceptor) {
        this.aspectjAopInterceptor = aspectjAopInterceptor;
    }

    //定义切点
    @Pointcut("execution(public !void *..*.*(..)) && @annotation(cache)")
    public void cachePointcut(Cache cache) {
    }

    //定义切点
    @Pointcut("execution(public * *(..)) && @annotation(cacheDelete)")
    public void cacheDeletePointcut(CacheDelete cacheDelete) {
    }


    @Around(value = "cachePointcut(cache)", argNames = "jp,cache")
    public Object cache(ProceedingJoinPoint jp, Cache cache) {
        try {
            return aspectjAopInterceptor.proceed(jp, cache);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @Around(value = "cacheDeletePointcut(cacheDelete)", argNames = "pjp,cacheDelete")
    public Object cacheDelete(ProceedingJoinPoint pjp, CacheDelete cacheDelete) {
        try {
            return aspectjAopInterceptor.cacheDelete(pjp, cacheDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
