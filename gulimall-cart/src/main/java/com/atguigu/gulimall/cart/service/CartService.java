package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * ClassName: CartService
 * Package: com.atguigu.gulimall.cart.service.impl
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/26 下午9:25
 * @Version 1.0
 */
public interface CartService {
    List<CartItemVo> getUserCartItems();

    CartVo getCart() throws ExecutionException, InterruptedException;

    CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItemVo getCartItem(Long skuId);

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteIdCartInfo(Integer skuId);

    void clearCartInfo(String cartKey);
}
