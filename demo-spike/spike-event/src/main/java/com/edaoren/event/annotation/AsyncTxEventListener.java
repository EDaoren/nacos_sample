package com.edaoren.event.annotation;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.*;

/**
 * Spring的异步事务事件监听器, 能够保证在事务提交后执行事件, 推荐使用
 * @author EDaoren
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Async
@TransactionalEventListener(fallbackExecution = true)
public @interface AsyncTxEventListener {
}