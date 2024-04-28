package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * ClassName: OrderWebConfiguration
 * Package: com.atguigu.gulimall.order.config
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/28 下午4:05
 * @Version 1.0
 */
@Configuration
@Slf4j
public class OrderWebConfiguration implements WebMvcConfigurer {

    @Resource
    private LoginUserInterceptor loginUserInterceptor;

    /**
     * 向拦截器注册表中添加拦截器。
     * 该方法会将指定的拦截器添加到拦截器链中，使其对所有请求生效。
     *
     * @param registry 拦截器注册表，用于注册和管理拦截器。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 将loginUserInterceptor拦截器添加到注册表中，并设置其拦截所有请求
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
    }
}
