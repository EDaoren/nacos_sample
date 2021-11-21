package com.edaoren.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edaoren.api.entity.User;
import com.edaoren.api.service.AsyncTaskService;
import com.edaoren.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author EDaoren
 */
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    @Autowired
    private UserMapper userMapper;


    @Override
    @Async("taskExecutor")
    public void executeAsyncTask(Map<String, Object> map, CountDownLatch cdl) {
        long start = System.currentTimeMillis();
        // 导出文件路径
        List<User> list = new ArrayList<>();
        try {
            Integer cur = (Integer) map.get("page");
            Integer pageSize = (Integer) map.get("limit");
            IPage<User> userPage = new Page<>(cur, pageSize);//参数一是当前页，参数二是每页个数
            //查询要导出的批次数据
            userPage = userMapper.selectPage(userPage, null);
            list = userPage.getRecords();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 写法1
        String filepath = map.get("path").toString() + map.get("page") + ".xlsx";
        File zip = new File(filepath);
        try {
            zip.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 这里 需要指定写用哪个class去读，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        // 如果这里想使用03 则 传入excelType参数即可
        EasyExcel.write(filepath, User.class).sheet("模板").doWrite(list);
        long end = System.currentTimeMillis();
        System.out.println("线程：" + Thread.currentThread().getName() + " , 导出excel   " + map.get("page") + ".xlsx   成功 , 导出数据 " + list.size() + " ,耗时 ：" + (end - start));
        list.clear();
        //执行完毕线程数减一
        cdl.countDown();
        System.out.println("剩余任务数  ===========================> " + cdl.getCount());

    }
}
