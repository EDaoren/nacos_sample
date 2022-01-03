package com.edaoren;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author EDaoren
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableDubbo(scanBasePackages = "com.edaoren")
@MapperScan(basePackages = "com.edaoren.mapper")
public class SpikeConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpikeConsumerApplication.class, args);
    }

}
