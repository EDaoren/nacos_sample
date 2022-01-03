package com.edaoren.utils;

import org.springframework.context.ApplicationEvent;

public class UnlockEvent extends ApplicationEvent {

    /**
     *
     */
    private static final long serialVersionUID = 6787676352564192324L;

    private UnlockEvent(Object source) {
        super(source);
    }

    public static UnlockEvent build(Object source) {
        return new UnlockEvent(source);
    }

}
