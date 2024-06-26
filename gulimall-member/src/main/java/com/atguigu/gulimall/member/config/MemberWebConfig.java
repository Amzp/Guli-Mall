package com.atguigu.gulimall.member.config;

import com.atguigu.gulimall.member.interceptor.LoginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: Rain^
 * @createTime: 2020-07-08 14:28
 **/

@Configuration
public class MemberWebConfig implements WebMvcConfigurer {

    @Resource
    private LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
    }

}
