package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * ClassName: GuliFeignConfig
 * Package: com.atguigu.gulimall.order.config
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/28 下午6:16
 * @Version 1.0
 */
@Configuration
@Slf4j
public class GuliFeignConfig {

    /**
     * 创建并返回一个请求拦截器 Bean。
     * 这个拦截器用于在发送请求之前，将当前请求的Cookie头同步到请求模板中。
     *
     * @return RequestInterceptor 请求拦截器实例
     */
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {

        return template -> {
            // 拦截请求，进行请求头的同步，特别是Cookie
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            log.debug("拦截请求，进行请求头的同步，特别是Cookie，当前线程id = {}", Thread.currentThread().getId());

            if (requestAttributes != null) {
                // 获取当前的 HttpServletRequest 对象
                HttpServletRequest request = requestAttributes.getRequest();

                // 同步请求头的数据，特别是从老请求中获取Cookie并同步到新请求上
                String cookie = request.getHeader("Cookie");
                template.header("Cookie", cookie);
            }
        };
    }

}
