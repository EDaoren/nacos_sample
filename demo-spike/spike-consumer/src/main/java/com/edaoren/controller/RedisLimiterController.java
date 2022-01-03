package com.edaoren.controller;

import cn.hutool.core.util.RandomUtil;
import com.edaoren.api.annotation.Limiter;
import com.edaoren.api.enums.LimiterTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author EDaoren
 */
@Slf4j
@RestController
@RequestMapping("/redisLimiter")
public class RedisLimiterController {

    @Limiter(type = LimiterTypeEnum.PATH)
    @PostMapping("/limiterByPath")
    public String limiterByPath() {
        log.info(RandomUtil.randomNumbers(6));
        return "SUCCESS";
    }

    @Limiter(type = LimiterTypeEnum.USER, burstCapacity = 1)
    @GetMapping("/limiterByUserId")
    public String limiterByUserId() {
        return "SUCCESS";
    }

    @Limiter(type = LimiterTypeEnum.IP, replenishRate = 2)
    @GetMapping("/limiterByIp")
    public String limiterByIp() {
        return "SUCCESS";
    }

}
