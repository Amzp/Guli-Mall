package com.atguigu.gulimall.cart.controller;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * ClassName: CartController
 * Package: com.atguigu.gulimall.cart.controller
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/26 下午9:36
 * @Version 1.0
 */
@Controller
@Slf4j
public class CartController {

    @GetMapping("/cart.html")
    public String cartListPage() {

        // 1. 快速得到用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        log.info("userInfoTo: {}", userInfoTo);

        return "cartList";
    }
}
