package com.mingri;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Slf4j
@SpringBootApplication
@EnableCaching//开发缓存注解功能
@EnableTransactionManagement //开启注解方式的事务管理
@Configurable
public class ApplicationRun {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationRun.class, args);
        log.info("server started");
    }
}
