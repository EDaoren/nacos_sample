package com.edaoren.event.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发送ampq消息队列的工具
 *
 * @author EDaoren
 */
@Slf4j
@Component
public class AmqpSendMsgUtil {

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 发送mq消息
     *
     * @param routingKey  路由键
     * @param messageBody 消息实体
     * @return
     */
    public boolean convertAndSend(String routingKey, Object messageBody) {
        try {
            amqpTemplate.convertAndSend(routingKey, messageBody);
            log.debug("发送消息成功, routingKey: {}, message: {}", routingKey, messageBody);
        } catch (AmqpException e) {
            e.printStackTrace();
            log.error("发送消息失败,routingKey: {}, message: {}", routingKey, messageBody);
            return false;
        }
        return true;
    }

    /**
     * 发送mq消息
     *
     * @param exchange    交换机
     * @param routingKey  路由键
     * @param messageBody 消息实体
     */
    public boolean convertAndSend(String exchange, String routingKey, Object messageBody) {
        try {
            amqpTemplate.convertAndSend(exchange, routingKey, messageBody);
            log.debug("发送消息成功, exchange: {}, routingKey: {}, message: {}", exchange, routingKey, messageBody);
        } catch (AmqpException e) {
            e.printStackTrace();
            log.error("发送消息失败, exchange: {}, routingKey: {}, message: {}", exchange, routingKey, messageBody);
            return false;
        }
        return true;
    }

    /**
     * 发送延迟 MQ 消息
     *
     * @param exchange    交换机
     * @param routingKey  路由键
     * @param messageBody 消息实体
     * @param time        延迟时间（单位毫秒）
     * @return
     */
    public boolean convertAndDelaySend(String exchange, String routingKey, Object messageBody, Integer time) {
        try {

            amqpTemplate.convertAndSend(exchange, routingKey, messageBody, message -> {
                message.getMessageProperties().setDelay(time);
                return message;
            });
            log.debug("发送延时消息成功, exchange: {}, routingKey: {}, message: {}, time: {}", exchange, routingKey, messageBody, time);
        } catch (AmqpException e) {
            e.printStackTrace();
            log.error("发送延时消息失败, exchange: {}, routingKey: {}, message: {}, time: {}", exchange, routingKey, messageBody, time);
            return false;
        }
        return true;
    }
}
