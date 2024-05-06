package com.atguigu.gulimall.ware;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Arrays;


@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
//@SpringBootApplication
@SpringBootApplication(exclude = GlobalTransactionAutoConfiguration.class)
@Slf4j
public class GulimallWareApplication {

    public static void main(String[] args) {
        log.info("开始启动Gulimall仓库服务应用，args = {}", Arrays.toString(args));
        SpringApplication.run(GulimallWareApplication.class, args);
        log.info("Gulimall仓库服务应用启动成功，args = {}", Arrays.toString(args));
    }

}
