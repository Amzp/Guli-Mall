package com.atguigu.gulimall.cart.service.impl;

import com.atguigu.gulimall.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * ClassName: CartServiceImpl
 * Package: com.atguigu.gulimall.cart.service.impl
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/26 下午9:26
 * @Version 1.0
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

}
