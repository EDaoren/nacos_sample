package com.edaoren.controller;

import com.edaoren.api.dto.ExportParamDTO;
import com.edaoren.api.entity.User;
import com.edaoren.api.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author EDaoren
 */
@RestController
public class UserController {

    @DubboReference(timeout = 100000, retries = 0)
    private UserService userService;


    @GetMapping("/getById/{id}")
    public User getById(@PathVariable Long id) {
        System.out.println("查询用户");
        User user = userService.getUserById(id);
        return user;
    }


    @GetMapping("/excelUser")
    public String excelUser(HttpServletResponse response) {
        int size = 40000;
        String url = "http://localhost/fileName.zip";


        ExportParamDTO exportParamDTO = new ExportParamDTO();
        String fileName = userService.excelUser(exportParamDTO);
        url = url.replace("fileName", fileName);
        return url;
    }

    @GetMapping("/selectBigData")
    public String selectBigData() {
        userService.queryBigData();
        return "成功";
    }
}
