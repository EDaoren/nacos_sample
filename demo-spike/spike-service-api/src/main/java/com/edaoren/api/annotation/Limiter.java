package com.edaoren.api.annotation;

import com.edaoren.api.enums.LimiterTypeEnum;

import java.lang.annotation.*;


/**
 * @author EDaoren
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Limiter {

    LimiterTypeEnum type() default LimiterTypeEnum.PATH;

    /**
     * 每秒生产令牌数量
     *
     * @return
     */
    int replenishRate() default 1;

    /**
     * 桶容量（并发数量）
     *
     * @return
     */
    int burstCapacity() default 2;

    /**
     * 每次请求消耗令牌数量
     *
     * @return
     */
    int requestedTokens() default 1;
}
