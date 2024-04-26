package com.atguigu.gulimall.cart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.util.Arrays;

/**
 * Gulimall购物车应用的主类。
 * 该类定义了应用的入口点，并通过注解配置了应用的相关特性。
 *
 * @EnableRedisHttpSession 启用基于Redis的HTTP会话管理，用于管理用户购物车数据。
 * @EnableFeignClients 启用Feign客户端，用于微服务之间的调用。
 * @EnableDiscoveryClient 启用服务发现客户端，用于从注册中心发现其他服务。
 * @SpringBootApplication(exclude = DataSourceAutoConfiguration.class) 定义Spring Boot应用，排除数据源自动配置，避免不必要的数据库配置。
 * @Slf4j 使用SLF4J日志注解，为类提供日志记录能力。
 */
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@Slf4j
public class GulimallCartApplication {

    /**
     * 应用的入口点。
     * @param args 命令行传入的参数数组。
     */
    public static void main(String[] args) {
        log.info("开始启动Gulimall-购物车应用：args = {}", Arrays.toString(args)); // 记录应用启动信息
        SpringApplication.run(GulimallCartApplication.class, args); // 启动Spring Boot应用
        log.info("Gulimall-购物车应用启动成功..."); // 记录应用启动成功信息
    }

}
