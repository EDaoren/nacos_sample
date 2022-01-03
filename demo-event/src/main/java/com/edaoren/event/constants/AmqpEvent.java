package com.edaoren.event.constants;

import lombok.Data;

/**
 * AMQP事件
 *
 * @param <T> 消息体泛型
 * @author EDaoren
 */
@Data
public abstract class AmqpEvent<T> {

    /**
     * 事件消息
     */
    protected T message;

    public AmqpEvent(T message) {
        this.message = message;
    }

    public T getMessage() {
        return message;
    }

    /**
     * 获取交换机
     *
     * @return
     */
    public abstract String getExchange();

    /**
     * 获取队列
     *
     * @return
     */
    public abstract String getQueue();

    /**
     * 获取路由键
     *
     * @return
     */
    public abstract String getRoutingKey();
}
