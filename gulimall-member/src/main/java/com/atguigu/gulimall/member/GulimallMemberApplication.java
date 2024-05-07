package com.atguigu.gulimall.member;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、想要远程调用别的服务
 * 1）、引入open-feign
 * 2）、编写一个接口，告诉SpringCloud这个接口需要调用远程服务
 *      2.1、声明接口的每一个方法都是调用哪个远程服务的那个请求
 * 3）、开启远程调用功能
 */
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.atguigu.gulimall.member.feign") // 开启远程调用功能
@EnableDiscoveryClient  // 开启nacos注册发现功能
@SpringBootApplication
@Slf4j
public class GulimallMemberApplication {

    public static void main(String[] args) {
        log.info("谷粒商城会员服务开始启动...");
        SpringApplication.run(GulimallMemberApplication.class, args);
        log.info("谷粒商城会员服务启动成功...");
    }

}
