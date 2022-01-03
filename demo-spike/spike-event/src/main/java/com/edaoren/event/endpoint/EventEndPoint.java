package com.edaoren.event.endpoint;

import com.edaoren.event.constants.CommonResult;

/**
 * 发布事件的入口,为了将一些复杂的逻辑从基本的业务逻辑中抽取出来，因此增加多一层抽象层，除了基本的逻辑之外，用于封装一些异步的，对外的请求的业务逻辑
 *
 * @author EDaoren
 */
public interface EventEndPoint<T> {

    /**
     * 、发布事件，用于系统解耦，采用MQ或者Spring的异步消息同步
     *
     * @param source 消息来源
     * @return
     */
    CommonResult publish(T source);
}
