package com.edaoren.event.amqp;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.edaoren.event.message.TestMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * 测试Mq监听
 *
 * @author EDaoren
 */
@Slf4j
@Component
public class TestRabbitListener {
    
    /**
     * 监听
     *
     * @param msg
     */
   /* @RabbitListener(bindings = @QueueBinding(value = @Queue(value = TestAmqpEvent.QUEUE, durable = "true"),
            exchange = @Exchange(value = TestAmqpEvent.EXCHANGE),
            key = {TestAmqpEvent.ROUTING_KEY}))
    public void onCreateListener(TestMessage msg, Message message, Channel channel) throws IOException {
        log.info("接收时间: {}, 消息: {}", System.currentTimeMillis(), JSONUtil.toJsonStr(msg));

        //手动签收消息
        //channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }*/


    /**
     * 延时队列监听
     *
     * @param msg
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = TestAmqpEvent.QUEUE, durable = "true"),
            exchange = @Exchange(value = TestAmqpEvent.EXCHANGE,
                    arguments = {@Argument(name = "x-delayed-type", value = "direct"),
                    }, delayed = Exchange.TRUE),
            key = {TestAmqpEvent.ROUTING_KEY}), ackMode = "MANUAL")
    public void onListener(TestMessage msg, Message message, Channel channel) throws IOException {
        log.info("供延时队列消费，接收时间: {}, 消息: {}", DateUtil.formatDateTime(new Date()), JSONUtil.toJsonStr(msg));
        //手动签收消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
