package com.atguigu.gulimall.cart.config;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ClassName: GulimallWebConfig
 * Package: com.atguigu.gulimall.cart.config
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/26 下午10:11
 * @Version 1.0
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}
