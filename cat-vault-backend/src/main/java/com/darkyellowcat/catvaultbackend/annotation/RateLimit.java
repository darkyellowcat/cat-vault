package com.darkyellowcat.catvaultbackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 时间窗口内最大请求数
     */
    int maxRequests() default 10;

    /**
     * 时间窗口（秒）
     */
    int timeWindow() default 60;

    /**
     * 限流维度：ip / user
     */
    String keyType() default "ip";
}
