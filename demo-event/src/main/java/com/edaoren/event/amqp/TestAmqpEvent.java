package com.edaoren.event.amqp;

import com.edaoren.event.constants.AmqpEvent;
import com.edaoren.event.message.TestMessage;

/**
 * @author EDaoren
 */
public class TestAmqpEvent extends AmqpEvent<TestMessage> {

    /**
     * 交换机
     */
    public final static String EXCHANGE = "test";

    /**
     * 队列名
     */
    public final static String QUEUE = EXCHANGE + ".mq";

    /**
     * 路由键
     */
    public final static String ROUTING_KEY = QUEUE;


    public TestAmqpEvent(TestMessage message) {
        super(message);
    }

    @Override
    public String getExchange() {
        return EXCHANGE;
    }

    @Override
    public String getQueue() {
        return QUEUE;
    }

    @Override
    public String getRoutingKey() {
        return ROUTING_KEY;
    }
}
