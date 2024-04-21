package com.atguigu.gulimall.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * @author Jerry
 */

//@EnableRedisHttpSession
//@EnableFeignClients
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@Slf4j
public class GulimallSearchApplication {

  public static void main(String[] args) {
    log.info("开始启动Gulimall搜索服务应用...");
    SpringApplication.run(GulimallSearchApplication.class, args);
    log.info("Gulimall搜索服务应用启动成功...");
  }

}
