package com.atguigu.gulimall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@MapperScan("com.atguigu.gulimall.ware.dao")
@Configuration
@Slf4j
public class WareMyBatisConfig {

    /**
     * 创建并配置分页拦截器 Bean。
     * 分页拦截器用于对 Spring Boot 的 Web 项目中的分页查询进行拦截，以实现分页效果。
     *
     * @return PaginationInterceptor 分页拦截器实例
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        log.info("配置分页拦截器");
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 配置分页参数
        // 禁止设置最大页数溢出，即当请求的页数大于设置的最大页数时的行为。
        // true 表示回到首页，false 表示继续请求，默认为 false。
//        paginationInterceptor.setOverflow(true);
        // 设置每页最多可显示的数量。默认为 500，-1 表示不限制。
//        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }

}
