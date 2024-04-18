package com.atguigu.gulimall.ware;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@Slf4j
public class GulimallWareApplication {

    public static void main(String[] args) {
        log.info("开始启动Gulimall仓库服务应用...");
        SpringApplication.run(GulimallWareApplication.class, args);
        log.info("Gulimall仓库服务应用启动成功...");
    }

}
