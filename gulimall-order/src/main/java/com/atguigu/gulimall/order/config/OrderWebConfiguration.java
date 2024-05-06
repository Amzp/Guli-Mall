package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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
        registry.addInterceptor(loginUserInterceptor)
                .addPathPatterns("/**")
                // 排除Excel下载接口，允许无token访问
                .excludePathPatterns("/user/downloadExcel")
                // 排除Swagger相关资源，以便在未登录状态下也能查看和测试API
                .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");
    }

    /**
     * 自定义静态资源处理器，以支持Swagger UI的显示和WebJars资源的加载。
     * 此配置将映射Swagger UI的HTML页面和WebJars资源到类路径中的特定位置。
     *
     * @param registry ResourceHandlerRegistry 实例，用于管理Spring MVC的静态资源处理器。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射"swagger-ui.html"请求到类路径的"META-INF/resources/"下，以加载Swagger UI的HTML页面
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        // 映射"/webjars/**"请求到类路径的"META-INF/resources/webjars/"下，以加载WebJars资源（如Bootstrap、jQuery等）
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
