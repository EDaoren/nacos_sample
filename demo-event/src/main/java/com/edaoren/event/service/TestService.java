package com.edaoren.event.service;

import cn.hutool.core.date.DateUtil;
import com.edaoren.event.amqp.TestAmqpEvent;
import com.edaoren.event.constants.CommonResult;
import com.edaoren.event.message.TestMessage;
import com.edaoren.event.publisher.AmqpEventPublisher;
import com.edaoren.event.publisher.SpringEventPublisher;
import com.edaoren.event.spring.TestSpringEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 测试服务
 *
 * @author EDaoren
 */
@Slf4j
@Service
public class TestService {

    @Autowired
    private AmqpEventPublisher amqpEventPublisher;

    @Autowired
    private SpringEventPublisher springEventPublisher;


    /**
     * 测试发送MQ消息
     *
     * @return
     */
    @Transactional
    public CommonResult<String> testAmqp() throws Exception {
        log.info("发送测试消息, 时间：{}", DateUtil.formatDateTime(new Date()));
        amqpEventPublisher.publish(new TestAmqpEvent(TestMessage.builder().content("测试消息").build()),
                10L, ChronoUnit.SECONDS, true);
        return CommonResult.success();
    }

    /**
     * 测试发送MQ消息
     *
     * @return
     */
    @Transactional
    public CommonResult<String> testSpring() {
        log.info("发送测试消息, 时间：{}", DateUtil.formatDateTime(new Date()));
        springEventPublisher.publish(new TestSpringEvent(TestMessage.builder().content("测试消息").build()));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CommonResult.success();
    }

}
