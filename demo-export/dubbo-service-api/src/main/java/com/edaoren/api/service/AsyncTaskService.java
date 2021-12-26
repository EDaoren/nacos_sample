package com.edaoren.api.service;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author EDaoren
 */
public interface AsyncTaskService {

    /**
     * 分批次异步导出数据
     *
     * @param map 要导出的批次数据信息
     * @param cdl countDownLatch这个类使一个线程等待其他线程各自执行完毕后再执行。
     */
    void executeAsyncTask(Map<String, Object> map, CountDownLatch cdl);
}
