package com.atguigu.gulimall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement //开启事务
@MapperScan("com.atguigu.gulimall.product.dao")
public class MyBatisConfig {

    /**
     * 创建并配置分页拦截器 Bean。
     * 分页拦截器用于对 Spring Boot 的 Web 项目中的分页查询进行拦截，以实现分页效果。
     *
     * @return PaginationInterceptor 分页拦截器实例
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();

        // 配置分页参数
        // 设置当请求的页面大于最大页后的行为，true为回到首页，false为继续请求当前页
        paginationInterceptor.setOverflow(true);
        // 设置最大单页限制数量，默认为500条，设置为1000条以适应需求
        paginationInterceptor.setLimit(1000);

        return paginationInterceptor;
    }

}
