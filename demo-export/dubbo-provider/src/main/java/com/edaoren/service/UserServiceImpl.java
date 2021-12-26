package com.edaoren.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.edaoren.api.dto.ExportParamDTO;
import com.edaoren.api.entity.User;
import com.edaoren.api.service.AsyncTaskService;
import com.edaoren.api.service.UserService;
import com.edaoren.mapper.UserMapper;
import com.edaoren.utils.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * 用户服务
 *
 * @author EDaoren
 */
@Service
@DubboService
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 每批次处理的数据量
     */
    private static final int LIMIT = 50000;

    /**
     * Queue是java自己的队列，具体可看API，是同步安全的
     */
    public static Queue<Map<String, Object>> queue;

    static {
        queue = new ConcurrentLinkedQueue<Map<String, Object>>();
    }

    private String filePath = "G:\\export\\";


    @Autowired
    private AsyncTaskService asyncTaskService;

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }


    @Override
    public String excelUser(ExportParamDTO exportParamDTO) {
        //Integer total = userMapper.selectCount(null);
        initQueue(10000000);
        long start = System.currentTimeMillis();
        //异步转同步，等待所有线程都执行完毕返会 主线程才会结束
        try {
            CountDownLatch cdl = new CountDownLatch(queue.size());
            while (queue.size() > 0) {
                asyncTaskService.executeAsyncTask(queue.poll(), cdl);
            }
            cdl.await();
            //压缩文件
            File zipFile = new File(filePath.substring(0, filePath.length() - 1) + ".zip");
            FileOutputStream fos1 = new FileOutputStream(zipFile);
            //压缩文件目录
            ZipUtils.toZip(filePath, fos1, true);
            //发送zip包
            //ZipUtils.sendZip(exportParamDTO.getResponse(), zipFile);
        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        System.out.println("任务执行完毕       共消耗   ：  " + (end - start) / 1000 / 60 + "  分钟");
        return "导出成功";
    }

    /**
     * 初始化队列
     */
    public void initQueue(Integer total) {
        //循环次数
        int count = total / LIMIT + (total % LIMIT > 0 ? 1 : 0);
        for (int i = 1; i <= count; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("page", i);
            map.put("limit", LIMIT);
            map.put("path", filePath);
            //添加元素
            queue.offer(map);
        }
    }

    @Override
    public void queryBigData() {
        List<User> results = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper();
        userMapper.getOrgWithBigData(queryWrapper, resultContext -> {
            User resultObject = resultContext.getResultObject();
            results.add(resultObject);
        });
        System.out.println(results.size());
    }
}
