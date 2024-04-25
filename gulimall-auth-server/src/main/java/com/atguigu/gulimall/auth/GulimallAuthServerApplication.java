package com.atguigu.gulimall.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Arrays;


@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@Slf4j
public class GulimallAuthServerApplication {

	public static void main(String[] args) {
		log.info("开始启动Gulimall-认证中心（社交登录、OAuth2.0、单点登录）应用：args = {}", Arrays.toString(args));

		SpringApplication.run(GulimallAuthServerApplication.class, args);

		log.info("Gulimall-认证中心（社交登录、OAuth2.0、单点登录）应用启动成功");
	}

}
