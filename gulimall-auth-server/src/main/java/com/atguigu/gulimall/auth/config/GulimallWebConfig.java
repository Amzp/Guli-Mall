package com.atguigu.gulimall.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ClassName: GulimallWebConfig
 * Package: com.atguigu.gulimall.auth.config
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/24 下午2:40
 * @Version 1.0
 */
@Configuration
@Slf4j
public class GulimallWebConfig implements WebMvcConfigurer {

    /**
     * 添加视图控制器到注册表。
     * 该方法重写了addViewControllers方法，用于配置特定的URL路径到对应的视图。
     *
     * @param registry 视图控制器注册表，用于注册和管理视图控制器。
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        log.debug("添加视图控制器到注册表...");

        // 将"login.html"路径映射到"login"视图
//        registry.addViewController("login.html").setViewName("login");
//        log.debug("映射login.html到login视图...");

        // 将"reg.html"路径映射到"reg"视图
        registry.addViewController("reg.html").setViewName("reg");
        log.debug("映射reg.html到reg视图...");

    }
}
