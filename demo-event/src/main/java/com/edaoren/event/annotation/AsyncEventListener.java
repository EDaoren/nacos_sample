package com.edaoren.event.annotation;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.lang.annotation.*;

/**
 * spring 的 异步事件监听器, 不管当前处于哪个事务阶段, 请慎用
 *
 * @author EDaoren
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Async
@EventListener
public @interface AsyncEventListener {
}