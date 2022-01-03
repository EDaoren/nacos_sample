package com.edaoren.event.spring;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.edaoren.event.message.TestMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 测试Spring监听
 *
 * @author EDaoren
 */
@Slf4j
@Component
public class TestSpringListener {

    @EventListener
    //@TransactionalEventListener
    public void onListener(TestSpringEvent event) {
        TestMessage message = (TestMessage) event.getMessage();
        log.info("接收到Spring事件消息，接收时间: {}, 消息: {}", DateUtil.formatDateTime(new Date()), JSONUtil.toJsonStr(message));
    }
}
