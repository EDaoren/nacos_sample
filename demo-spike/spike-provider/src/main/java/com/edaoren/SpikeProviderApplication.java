package com.edaoren;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/**
 * @author EDaoren
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableDubbo(scanBasePackages = "com.edaoren")
@MapperScan(basePackages = "com.edaoren.mapper")
@Import(cn.hutool.extra.spring.SpringUtil.class)
public class SpikeProviderApplication {

    /**
     * 日志.
     */
    private final static Logger log = LoggerFactory.getLogger(SpikeProviderApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpikeProviderApplication.class, args);
    }

}

