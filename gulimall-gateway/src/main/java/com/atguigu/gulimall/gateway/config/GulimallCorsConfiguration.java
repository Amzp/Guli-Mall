package com.atguigu.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GulimallCorsConfiguration {

    /**
     * 创建并配置跨域资源共享（CORS）的Web过滤器。
     *
     * <p>此函数不接受任何参数，主要用于创建并配置CorsWebFilter实例。
     * 通过设置跨域配置，允许来自任何来源、任何方法和任何头部的请求，
     * 并允许凭证（如cookies）在跨域请求中传递。
     *
     * @return CorsWebFilter 跨域Web过滤器实例，用于应用中以处理跨域请求。
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        /*
          创建一个基于URL的CORS配置源。
          该源将用于提供CORS配置，配置将基于请求的URL进行匹配。
         */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        /*
          创建一个新的CORS配置对象。
          该配置对象将用于定义跨域资源共享的规则。
         */
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 配置跨域请求的详细规则
        corsConfiguration.addAllowedHeader("*"); // 允许所有请求头
        corsConfiguration.addAllowedMethod("*"); // 允许所有请求方法
        corsConfiguration.addAllowedOrigin("*"); // 允许所有来源
        corsConfiguration.setAllowCredentials(true); // 允许凭证

        // 将跨域配置注册到URL基础上的源，应用到所有路径
        source.registerCorsConfiguration("/**", corsConfiguration);
        // 返回跨域过滤器实例
        return new CorsWebFilter(source);
    }

}
