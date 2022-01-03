package com.edaoren.controller;

import com.edaoren.api.dto.ExportParamDTO;
import com.edaoren.api.entity.User;
import com.edaoren.api.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author EDaoren
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @DubboReference(timeout = 100000, retries = 0)
    private UserService userService;


    @GetMapping("/getById/{id}")
    public User getById(@PathVariable Long id) {
        System.out.println("查询用户");
        User user = userService.getUserById(id);
        return user;
    }

}
