package com.atguigu.gulimall.product.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: ThreadPoolConfigProperties
 * Package: com.atguigu.gulimall.product.config
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/23 下午9:53
 * @Version 1.0
 */
/**
 * 线程池配置属性类，用于配置线程池的核心大小、最大大小和保持活动时间。
 * 该类使用了Lombok的数据注解简化了getter和setter的生成，并通过@ConfigurationProperties注解与Spring Boot的配置文件绑定。
 *
 * @ConfigurationProperties(prefix = "gulimall.thread") 用于指定配置前缀，使得在配置文件中通过"gulimall.thread"来配置该类的属性。
 * @Component 表示该类是一个组件，可被Spring容器管理。
 * @Data 提供了所有字段的getter和setter方法，简化了访问器的编写。
 * @Accessors(chain = true) 使得链式调用getter和setter方法成为可能。
 */
@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
@Accessors(chain = true)
public class ThreadPoolConfigProperties {
    private Integer coreSize; // 线程池的核心大小
    private Integer maxSize; // 线程池的最大大小
    private Integer keepAliveTime; // 线程池中空闲线程的保持活动时间
}
