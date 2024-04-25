package com.atguigu.gulimall.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * 1、开启服务注册发现
 *      (配置nacos的注册中心地址)
 * 2、编写网关配置文件
 */
@EnableDiscoveryClient  // 开启服务注册发现
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})    // 排除数据源自动配置
@Slf4j
public class GulimallGatewayApplication {

    public static void main(String[] args) {
        log.info("开始启动Gulimall网关服务应用...");
        SpringApplication.run(GulimallGatewayApplication.class, args);
        log.info("Gulimall网关服务应用启动成功...");
    }

}
