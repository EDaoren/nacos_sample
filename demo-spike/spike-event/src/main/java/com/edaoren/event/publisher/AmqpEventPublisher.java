package com.edaoren.event.publisher;

import com.edaoren.event.constants.AmqpEvent;
import com.edaoren.event.util.AmqpSendMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * AMQP事件发布者
 * PS: 支持在当前事务提交后再发送MQ, 如果想不在事务提交后, 请使用有 afterCommit 参数的方法, 传入afterCommit=false
 *
 * @author EDaoren
 */
@Component
public class AmqpEventPublisher {

    @Autowired
    private AmqpSendMsgUtil amqpSendMsgUtil;


    /**
     * 发布事件, 默认事务提交后执行
     *
     * @param event 事件对象
     * @param <T>   事件消息体泛型
     */
    public <T> void publish(AmqpEvent<T> event) {
        this.publish(event, false, null, null, true);
    }

    /**
     * 发布事件
     *
     * @param event       事件对象
     * @param afterCommit 是否在当前事务提交后执行
     * @param <T>         事件消息体泛型
     */
    public <T> void publish(AmqpEvent<T> event, boolean afterCommit) {
        this.publish(event, false, null, null, afterCommit);
    }

    /**
     * 发布事件, 默认事务提交后执行
     *
     * @param event 事件对象
     * @param time  延时时间
     * @param unit  时间单位
     * @param <T>   事件消息体泛型
     */
    public <T> void publish(AmqpEvent<T> event, Long time, ChronoUnit unit) {
        this.publish(event, true, time, unit, true);
    }

    /**
     * 发布事件
     *
     * @param event       事件对象
     * @param time        延时时间
     * @param unit        时间单位
     * @param afterCommit 是否在当前事务提交后执行
     * @param <T>         事件消息体泛型
     */
    public <T> void publish(AmqpEvent<T> event, Long time, ChronoUnit unit, boolean afterCommit) {
        this.publish(event, true, time, unit, afterCommit);
    }

    /**
     * 发布事件, 默认事务提交后执行
     *
     * @param events 事件对象集合
     * @param <T>    事件消息体泛型
     */
    public <T> void publish(List<AmqpEvent<T>> events) {
        this.publish(events, false, null, null, true);
    }

    /**
     * 发布事件
     *
     * @param events      事件对象集合
     * @param afterCommit 是否在当前事务提交后执行
     * @param <T>         事件消息体泛型
     */
    public <T> void publish(List<AmqpEvent<T>> events, boolean afterCommit) {
        this.publish(events, false, null, null, afterCommit);
    }

    /**
     * 发布事件, 默认事务提交后执行
     *
     * @param events 事件对象集合
     * @param time   延时时间
     * @param unit   时间单位
     * @param <T>    事件消息体泛型
     */
    public <T> void publish(List<AmqpEvent<T>> events, Long time, ChronoUnit unit) {
        this.publish(events, true, time, unit, true);
    }

    /**
     * 发布事件
     *
     * @param events      事件对象集合
     * @param time        延时时间
     * @param unit        时间单位
     * @param afterCommit 是否在当前事务提交后执行
     * @param <T>         事件消息体泛型
     */
    public <T> void publish(List<AmqpEvent<T>> events, Long time, ChronoUnit unit, boolean afterCommit) {
        this.publish(events, true, time, unit, afterCommit);
    }


    /**
     * 发布事件
     *
     * @param event       事件对象
     * @param delay       是否延时
     * @param time        延时时间
     * @param unit        时间单位
     * @param afterCommit 是否在当前事务提交后执行
     * @param <T>         事件消息体泛型
     */
    private <T> void publish(AmqpEvent<T> event, boolean delay, Long time, ChronoUnit unit, boolean afterCommit) {
        List<AmqpEvent<T>> events = new ArrayList<>();
        events.add(event);
        this.publish(events, delay, time, unit, afterCommit);
    }

    /**
     * 发布事件
     *
     * @param events      事件对象集合
     * @param delay       是否延时
     * @param time        延时时间
     * @param unit        时间单位
     * @param afterCommit 是否在当前事务提交后执行
     * @param <T>         事件消息体泛型
     */
    private <T> void publish(List<AmqpEvent<T>> events, boolean delay, Long time, ChronoUnit unit, boolean afterCommit) {
        Assert.notEmpty(events, "events not empty");

        // 如果是延时事件, 则要校验时间
        Duration duration = null;
        if (delay) {
            Assert.notNull(time, "time not null");
            Assert.notNull(unit, "unit not null");

            duration = Duration.of(time, unit);
            if (duration.toMillis() > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("超出int类型最大值, 只支持 < 4294967296 的延时值");
            }
        }

        AmqpEventPublisher that = this;

        // 说明当前有事务, 并且指定了要在事务提交后发送MQ
        if (TransactionSynchronizationManager.isSynchronizationActive() && afterCommit) {
            // 保证在事务提交后发送MQ
            Duration finalDuration = duration;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    that.publishEvent(events, delay, finalDuration);
                }
            });
        } else {
            that.publishEvent(events, delay, duration);
        }
    }

    /**
     * 发布事件
     *
     * @param events   事件对象集合
     * @param delay    是否延时
     * @param duration 延时时间
     * @param <T>      事件消息体泛型
     */
    private <T> void publishEvent(List<AmqpEvent<T>> events, boolean delay, Duration duration) {
        events.forEach(event -> {
            if (delay) {
                amqpSendMsgUtil.convertAndDelaySend(event.getExchange(), event.getRoutingKey(), event.getMessage(), (int) duration.toMillis());
            } else {
                amqpSendMsgUtil.convertAndSend(event.getExchange(), event.getRoutingKey(), event.getMessage());
            }
        });
    }

    /**
     * 延时发布事件, 用于批量发布事件的时候可以在前置控制流量, 以免形成大量消息在队列堆积
     *
     * @param events
     * @param <T>
     */
    private <T> void delayPublishEvent(List<AmqpEvent<T>> events, boolean delay, Duration duration) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            events.forEach(event -> {
                if (delay) {
                    amqpSendMsgUtil.convertAndDelaySend(event.getExchange(), event.getRoutingKey(), event.getMessage(), (int) duration.toMillis());
                } else {
                    amqpSendMsgUtil.convertAndSend(event.getExchange(), event.getRoutingKey(), event.getMessage());
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });
        });
    }

}
