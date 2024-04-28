package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.config.ThreadPoolConfigProperties;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

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

    @Resource
    private ProductFeignService productFeignService;

    @Resource
    private ThreadPoolExecutor executor;


    private final String CART_PREFIX = "gulimall:cart:";

    /**
     * 获取当前用户购物车中的商品列表。
     * 该方法首先从线程本地存储中获取用户信息，然后根据用户ID生成购物车的存储键。
     * 如果用户ID不存在，表示用户未登录，则返回null。
     * 如果用户已登录，将尝试从购物车存储中获取商品列表，并对这些商品进行过滤和加工：
     * 仅保留选中的商品，并通过远程调用获取每个商品的最新价格。
     *
     * @return 返回用户购物车中选中的商品列表。如果用户未登录或购物车中没有商品，则返回null。
     */
    @Override
    public List<CartItemVo> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get(); // 从线程本地存储获取用户信息
        if (userInfoTo.getUserId() == null) { // 判断用户是否已登录
            return null;
        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId(); // 根据用户ID生成购物车的存储键
            List<CartItemVo> cartItems = getCartItems(cartKey); // 从存储中获取购物车商品列表

            List<CartItemVo> collect = null;
            if (cartItems != null) { // 如果存在商品，则进行筛选和价格更新
                collect = cartItems.stream()
                        .filter(CartItemVo::getCheck) // 筛选选中的商品
                        .map(item -> {
                            R price = productFeignService.getPrice(item.getSkuId()); // 远程调用获取商品价格
                            item.setPrice(new BigDecimal(price.get("data").toString())); // 更新商品价格
                            return item;
                        })
                        .collect(Collectors.toList());
            }
            return collect; // 返回加工后的购物车商品列表
        }
    }

    /**
     * 获取用户的购物车信息。
     * 如果用户已登录，将返回该用户的购物车信息，包括合并过的临时购物车信息。
     * 如果用户未登录，将返回与该用户相关的临时购物车信息。
     *
     * @return CartVo 购物车信息的封装对象，包括购物车中的商品项列表。
     * @throws ExecutionException   当获取购物车数据时，如果存在执行异常则抛出。
     * @throws InterruptedException 当获取购物车数据的线程被中断时抛出。
     */
    @Override
    public CartVo getCart() throws ExecutionException, InterruptedException {
        CartVo cartVo = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        if (userInfoTo.getUserId() != null) {
            // 用户已登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            // 用于存储未登录时的临时购物车信息的键
            String temptCartKey = CART_PREFIX + userInfoTo.getUserKey();

            // 检查是否有临时购物车数据需要合并
            List<CartItemVo> tempCartItems = getCartItems(temptCartKey);
            if (tempCartItems != null) {
                // 合并临时购物车数据到登录用户的购物车中
                for (CartItemVo item : tempCartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
                // 清除临时购物车数据
                clearCartInfo(temptCartKey);
            }

            // 获取并设置登录用户的购物车数据
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);

        } else {
            // 用户未登录
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            // 获取并设置临时购物车数据
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
        }
        return cartVo;
    }

    @Override
    public void clearCartInfo(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    /**
     * 获取购物车里面的数据
     *
     * @param cartKey 购物车的键值，用于从Redis中获取购物车数据
     * @return 返回购物车中的商品列表，如果购物车中没有商品则返回null
     */
    private List<CartItemVo> getCartItems(String cartKey) {
        // 通过键值获取购物车操作对象
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(cartKey);
        // 获取购物车中的所有商品值
        List<Object> values = operations.values();
        if (values != null && !values.isEmpty()) {
            // 将JSON字符串转换为CartItemVo对象列表
            return values.stream()
                    .map((obj) -> JSON.parseObject((String) obj, CartItemVo.class)).collect(Collectors.toList());
        }
        // 如果购物车为空，则返回null
        return null;
    }


    /**
     * 将商品添加到购物车。
     *
     * @param skuId 商品ID，用于标识要添加到购物车的商品。
     * @param num   要添加的商品数量。
     * @return CartItemVo 购物车项的视图对象，包含添加商品的详细信息。
     * @throws ExecutionException   当异步任务执行完成时抛出的异常。
     * @throws InterruptedException 当异步任务被中断时抛出的异常。
     */
    @Override
    public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        // 判断Redis中是否存在该商品信息
        String productRedisValue = (String) cartOps.get(skuId.toString());
        // 如果不存在，则添加该商品信息
        if (StringUtils.isEmpty(productRedisValue)) {
            // 创建购物车项视图对象，并异步获取商品信息和销售属性
            CartItemVo cartItemVo = new CartItemVo();

            // 异步任务1：查询商品详细信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfoData = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItemVo.setSkuId(skuId)
                        .setTitle(skuInfoData.getSkuTitle())
                        .setImage(skuInfoData.getSkuDefaultImg())
                        .setPrice(skuInfoData.getPrice())
                        .setCount(num);
            }, executor);

            // 异步任务2：查询商品的销售属性组合
            CompletableFuture<Void> getSkuSaleAttrValuesTask = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttrValues(skuSaleAttrValues);
            }, executor);

            // 等待所有异步任务完成
            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValuesTask).get();

            // 将购物车项信息序列化并存储到Redis
            String cartItemJson = JSON.toJSONString(cartItemVo);
            cartOps.put(skuId.toString(), cartItemJson);

            return cartItemVo;
        } else {
            // 如果存在，则更新该商品的数量
            CartItemVo cartItemVo = JSON.parseObject(productRedisValue, CartItemVo.class);
            cartItemVo.setCount(cartItemVo.getCount() + num);

            // 更新Redis中的商品信息
            String cartItemJson = JSON.toJSONString(cartItemVo);
            cartOps.put(skuId.toString(), cartItemJson);

            return cartItemVo;
        }
    }

    /**
     * 获取购物车操作的绑定哈希操作对象。
     * 该方法会根据用户登录状态选择不同的Redis键来操作用户的购物车数据。
     * 如果用户已登录，使用用户ID作为键；如果用户未登录，使用用户唯一标识符（userKey）作为键。
     *
     * @return BoundHashOperations<String, Object, Object> 购物车数据的绑定哈希操作对象，允许对Redis中的哈希表进行操作，如添加、删除、更新条目。
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey;

        // 根据用户登录状态选择购物车键
        if (userInfoTo.getUserId() != null) {
            // 已登录用户，使用用户ID作为购物车键
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            // 未登录用户，使用用户Key作为购物车键
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        // 创建并返回购物车数据的绑定哈希操作对象
        return stringRedisTemplate.boundHashOps(cartKey);
    }


    /**
     * 获取指定SKU ID的购物车商品信息。
     *
     * @param skuId 商品的SKU编号，用于标识特定的商品。
     * @return CartItemVo 购物车商品的详细信息，包括商品的数量、规格等。
     */
    @Override
    public CartItemVo getCartItem(Long skuId) {
        // 获取与购物车相关的操作接口
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        // 从Redis中获取指定SKU ID的购物车商品信息（JSON格式）
        String redisValue = (String) cartOps.get(skuId.toString());

        // 将JSON格式的购物车商品信息反序列化为CartItemVo对象
        return JSON.parseObject(redisValue, CartItemVo.class);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        //查询购物车里面的商品
        CartItemVo cartItem = getCartItem(skuId);
        //修改商品状态
        cartItem.setCheck(check == 1);

        //序列化存入redis中
        String redisValue = JSON.toJSONString(cartItem);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), redisValue);

    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        //查询购物车里面的商品
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //序列化存入redis中
        String redisValue = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), redisValue);
    }

    @Override
    public void deleteIdCartInfo(Integer skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }
}
