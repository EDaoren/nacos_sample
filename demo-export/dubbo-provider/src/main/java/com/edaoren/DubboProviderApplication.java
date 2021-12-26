package com.edaoren;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author EDaoren
 */
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.edaoren")
@MapperScan(basePackages = "com.edaoren.mapper")
public class DubboProviderApplication {

    /**
     * 日志.
     */
    private final static Logger log = LoggerFactory.getLogger(DubboProviderApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DubboProviderApplication.class, args);
    }

}

