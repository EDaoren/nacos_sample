package com.edaoren.listener;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.edaoren.api.dto.RestResult;
import com.edaoren.api.message.SpikeOrderMessage;
import com.edaoren.api.service.SpikeService;
import com.edaoren.event.amqp.TestAmqpEvent;
import com.edaoren.event.message.TestMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SpikeOrderRabbitListener {

    @Autowired
    private SpikeService spikeService;

    /**
     * 监听
     *
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = SpikeOrderAmqpEvent.QUEUE, durable = "true"),
            exchange = @Exchange(value = SpikeOrderAmqpEvent.EXCHANGE),
            key = {SpikeOrderAmqpEvent.ROUTING_KEY}))
    public void onCreateListener(SpikeOrderMessage msg, Message message, Channel channel) throws IOException {
        log.info("接收时间: {}, 消息: {}", System.currentTimeMillis(), JSONUtil.toJsonStr(msg));
        RestResult<String> restResult = spikeService.rushBuySpikeActivityProcess(msg);
        log.info("消息处理结果：{}", JSONUtil.toJsonStr(restResult));
    }


    /**
     * 延时队列监听
     *
     * @param msg
     * @param message
     * @param channel
     * @throws IOException
     */
    /*@RabbitListener(bindings = @QueueBinding(value = @Queue(value = SpikeOrderAmqpEvent.QUEUE, durable = "true"),
            exchange = @Exchange(value = SpikeOrderAmqpEvent.EXCHANGE,
                    arguments = {@Argument(name = "x-delayed-type", value = "direct"),
                    }, delayed = Exchange.TRUE),
            key = {SpikeOrderAmqpEvent.ROUTING_KEY}), ackMode = "MANUAL")
    public void onListener(TestMessage msg, Message message, Channel channel) throws IOException {
        log.info("供延时队列消费，接收时间: {}, 消息: {}", DateUtil.formatDateTime(new Date()), JSONUtil.toJsonStr(msg));
        //手动签收消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }*/
}
