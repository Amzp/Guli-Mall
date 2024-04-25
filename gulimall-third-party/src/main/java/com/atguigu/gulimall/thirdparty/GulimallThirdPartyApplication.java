package com.atguigu.gulimall.thirdparty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@ComponentScan(basePackages = "com.atguigu.gulimall")
@SpringBootApplication
@Slf4j
public class GulimallThirdPartyApplication {

    public static void main(String[] args) {
        log.info("开始启动Gulimall第三方服务应用...");
        SpringApplication.run(GulimallThirdPartyApplication.class, args);
        log.info("Gulimall第三方服务应用启动成功...");
    }

}
