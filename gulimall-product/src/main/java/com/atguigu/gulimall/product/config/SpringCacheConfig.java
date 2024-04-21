package com.atguigu.gulimall.product.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;

/**
 * ClassName: SpringCacheConfig
 * Package: com.atguigu.gulimall.product.config
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/21 下午4:39
 * @Version 1.0
 */
@EnableConfigurationProperties(CacheProperties.class)
@Configuration
@EnableCaching
@Slf4j
public class SpringCacheConfig {


    /**
     * 初始化并配置Redis缓存配置。
     * 使用Spring Cache管理Redis缓存，配置缓存的序列化方式、TTL、键前缀等属性。
     *
     * @param cacheProperties 缓存配置属性，用于读取和配置Redis缓存的详细设置。
     * @return RedisCacheConfiguration Redis缓存配置对象，配置了键值序列化方式、TTL、键前缀等。
     */
    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        // 初始化Redis缓存的基本配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        log.info("初始化SpringCache Redis缓存配置...");

        // 配置键的序列化方式为StringRedisSerializer
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        log.info("配置键的序列化方式为StringRedisSerializer...");

        // 配置值的序列化方式为GenericJackson2JsonRedisSerializer，支持JSON格式的序列化和反序列化
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        log.info("配置值的序列化方式为GenericJackson2JsonRedisSerializer，用于支持JSON格式的序列化和反序列化...");

        // 根据配置文件中的设置，应用TTL、键前缀、是否缓存null值、是否使用键前缀等配置
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        // 应用TTL设置，如果TTL配置非空，则设置缓存的生存时间
        if(redisProperties.getTimeToLive() != null){
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        // 应用键前缀设置，如果配置了键前缀，则所有缓存键都会加上这个前缀
        if(redisProperties.getKeyPrefix() != null){
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        // 禁用缓存null值，如果配置禁用，则不允许缓存null值
        if(!redisProperties.isCacheNullValues()){
            config = config.disableCachingNullValues();
        }
        // 禁用键前缀，如果配置禁用，则不使用任何键前缀
        if(!redisProperties.isUseKeyPrefix()){
            config = config.disableKeyPrefix();
        }

        log.info("SpringCache Redis缓存配置初始化完成...");
        return config;
    }



}
