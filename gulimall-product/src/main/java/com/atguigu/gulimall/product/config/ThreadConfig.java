package com.atguigu.gulimall.product.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: ThreadConfig
 * Package: com.atguigu.gulimall.product.config
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/23 下午9:46
 * @Version 1.0
 */
//@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
@Configuration
@Slf4j
public class ThreadConfig {

    /**
     * 创建并配置一个 ThreadPoolExecutor 实例，以实现并发任务的高效管理和执行。
     *
     * @param pool ThreadPoolConfigProperties 实例，用于提供线程池的详细配置信息：
     *             - pool.getCoreSize()：获取线程池的基本核心线程数，即初始时保持活动状态的线程数。
     *             - pool.getMaxSize()：获取线程池允许的最大线程数，当队列满且核心线程数已达到时，会创建额外线程至该最大值。
     *             - pool.getKeepAliveTime()：获取线程空闲后的存活时间（单位：秒），超过该时间的空闲线程将被回收。
     *
     * @return 配置好的 ThreadPoolExecutor 实例，具备如下特性：
     * - 使用 LinkedBlockingDeque 作为工作队列，容量为 100,000，支持公平性、无界阻塞、FIFO 或 LIFO 排序。
     * - 使用 Executors.defaultThreadFactory() 作为线程工厂，创建新线程时使用默认命名规则和优先级。
     * - 设置拒绝策略为 ThreadPoolExecutor.AbortPolicy()，当线程池和队列已满，新提交的任务将抛出 RejectedExecutionException。
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool) {
        // 使用配置属性来初始化 ThreadPoolExecutor
        return new ThreadPoolExecutor(
                pool.getCoreSize(),
                pool.getMaxSize(),
                pool.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

    }

}
