package com.atguigu.gulimall.product.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;


@Configuration
@Slf4j
public class GulimallSessionConfig {

    /**
     * 配置Cookie序列化器
     * 无参数
     * @return 返回配置好的DefaultCookieSerializer实例
     */
    @Bean
    public CookieSerializer cookieSerializer() {

        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();

        // 设置Cookie的作用域为"gulimall.com"，并指定Cookie的名称为"GULISESSION"
        cookieSerializer.setDomainName("gulimall.com");
        cookieSerializer.setCookieName("GULISESSION");

        log.debug("CookieSerializer配置成功...");
        return cookieSerializer;
    }



    /**
     * 创建并返回一个用于Spring Session的默认Redis序列化器。
     * 这个序列化器使用Jackson 2 JSON库来将对象序列化为JSON格式，以便在Redis中存储。
     *
     * @return RedisSerializer<Object> 返回一个通用的Jackson 2 JSON Redis序列化器实例。
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        log.debug("RedisSerializer配置成功...");
        return new GenericJackson2JsonRedisSerializer();
    }

}
