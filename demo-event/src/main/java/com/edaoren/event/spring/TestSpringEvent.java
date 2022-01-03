package com.edaoren.event.spring;

import com.edaoren.event.constants.SpringEvent;
import com.edaoren.event.message.TestMessage;

/**
 * Spring EVENT 事件
 *
 * @author EDaoren
 */
public class TestSpringEvent extends SpringEvent<TestMessage> {


    public TestSpringEvent(TestMessage message) {
        super(message);
    }

    public TestSpringEvent(Object source, TestMessage message) {
        super(source, message);
    }
}
