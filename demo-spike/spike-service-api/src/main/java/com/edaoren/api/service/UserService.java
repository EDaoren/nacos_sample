package com.edaoren.api.service;

import com.edaoren.api.entity.User;

/**
 * 用户服务
 *
 * @author EDaoren
 */
public interface UserService {

    /**
     * 根据ID查询用户
     *
     * @param id
     * @return
     */
    User getUserById(Long id);
}
