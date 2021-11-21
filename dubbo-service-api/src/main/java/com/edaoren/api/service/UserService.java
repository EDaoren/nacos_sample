package com.edaoren.api.service;

import com.edaoren.api.dto.ExportParamDTO;
import com.edaoren.api.entity.User;

import javax.servlet.http.HttpServletResponse;

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

    /**
     * 导出用户
     *
     * @param size
     * @return
     */
    String excelUser(ExportParamDTO exportParamDTO);


    /**
     * 测试查找大数据
     */
    void queryBigData();
}
