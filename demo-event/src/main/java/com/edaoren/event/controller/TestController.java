package com.edaoren.event.controller;

import com.edaoren.event.constants.CommonResult;
import com.edaoren.event.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author EDaoren
 */
@RestController
@Slf4j
public class TestController {


    @Autowired
    private TestService testService;


    @GetMapping("/sendMqMsg")
    public String testAmqp() {
        try {
            CommonResult<String> commonResult = testService.testAmqp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "成功";
    }

    @PostMapping("/sendSpringMsg")
    public String testSpring() {
        CommonResult<String> commonResult = testService.testSpring();
        return "成功";
    }


}
