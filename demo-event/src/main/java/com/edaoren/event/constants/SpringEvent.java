package com.edaoren.event.constants;

import org.springframework.context.ApplicationEvent;

/**
 * Spring 事件
 *
 * @author EDaoren
 */
public abstract class SpringEvent<T> extends ApplicationEvent {

    protected T message;

    public SpringEvent(T message) {
        super(message);
        this.message = message;

    }

    public SpringEvent(Object source, T message) {
        super(source);
        this.message = message;
    }

    public T getMessage() {
        return message;
    }
}
