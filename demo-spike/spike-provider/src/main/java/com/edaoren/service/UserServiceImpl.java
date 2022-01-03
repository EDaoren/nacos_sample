package com.edaoren.service;

import com.edaoren.api.entity.User;
import com.edaoren.api.service.UserService;
import com.edaoren.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public User getUserById(Long id) {
        log.info("打点日志吧");
        return userMapper.selectById(id);
    }

}
