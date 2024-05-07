package com.atguigu.gulimall.product.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: RedissonConfig
 * Package: com.atguigu.gulimall.product.config
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/21 上午10:27
 * @Version 1.0
 */
@Configuration
@Slf4j
public class MyRedissonConfig {
    /**
     * 创建并配置Redisson客户端连接
     *
     * @return RedissonClient 返回一个配置好的Redisson客户端实例，用于与Redis服务器通信
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() {
        log.info("初始化RedissonClient配置...");

        // 初始化Redisson配置对象
        Config config = new Config();

        // 配置连接到单个Redis服务器的地址
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");

        log.info("RedissonClient配置完成...");
        // 根据配置创建并返回Redisson客户端实例
        return Redisson.create(config);
    }
}
