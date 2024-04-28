package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import com.atguigu.gulimall.order.vo.SkuStockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service("orderService")
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    private MemberFeignService memberFeignService;
    @Resource
    private CartFeignService cartFeignService;
    @Resource
    private WmsFeignService wmsFeignService;

    @Resource
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );
        return new PageUtils(page);
    }

    /**
     * 确认订单信息
     * 该方法会根据当前登录用户的信息，异步获取地址和购物车商品信息，用于生成订单确认页面的数据。
     *
     * @return OrderConfirmVo 返回一个包含用户地址和当前购物车商品信息的订单确认视图对象。
     */
    @Override
    public OrderConfirmVo confirmOrder() {
        // 初始化订单确认视图对象
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        // 获取当前登录的用户信息
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        log.debug("confirmOrder主线程 id = {}", Thread.currentThread().getId());

        // 获取请求属性，以便在异步任务中保持上下文
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 异步获取用户地址列表
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // 调用会员服务，获取用户地址列表
            log.debug("Address异步线程 id = {}", Thread.currentThread().getId());
            // 恢复当前线程的请求属性
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 调用远程服务获取地址列表，并设置到订单确认视图对象中
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setMemberAddressVos(address);
        }, executor);

        /**
         * 异步获取当前用户的购物车商品信息，并进一步获取这些商品的库存状态。
         * 该操作分为两个阶段：
         * 1. 首先，异步调用购物车服务，获取当前用户购物车中的商品列表，并将这些信息设置到订单确认视图对象中。
         * 2. 其次，在第一个阶段完成后，异步调用库存服务，查询购物车中商品的库存状况，并更新到订单确认视图对象中。
         *
         * @param confirmVo 订单确认视图对象，用于存储购物车商品信息和对应的库存状态。
         * @param requestAttributes 当前请求的属性，用于在异步线程中恢复请求上下文。
         * @param executor 用于执行异步任务的线程池。
         * @return CompletableFuture<Void> 表示异步操作完成的未来对象，不返回任何结果。
         */
        CompletableFuture<Void> getCartFuture = CompletableFuture
                .runAsync(() -> {
                    // 在异步线程中获取当前用户购物车中的商品列表
                    log.debug("Cart异步线程 id = {}", Thread.currentThread().getId());
                    // 恢复当前线程的请求属性，以确保在异步环境中能够保持正确的上下文环境
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                    // 调用远程购物车服务，获取当前用户购物车中的商品列表，并将这些商品信息设置到订单确认视图对象中
                    List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
                    confirmVo.setItems(currentCartItems);
                }, executor)
                .thenRunAsync(() -> {
                    // 基于订单确认视图对象中的商品信息，查询这些商品的库存状态
                    List<OrderItemVo> items = confirmVo.getItems();
                    List<Long> collect = items.stream()
                            .map(OrderItemVo::getSkuId)
                            .collect(Collectors.toList());
                    // 调用远程库存服务，查询购物车商品的库存状况
                    R hasStock = wmsFeignService.getSkuHasStock(collect);
                    // 解析库存服务的响应，将商品的库存状态设置到订单确认视图对象中
                    List<SkuStockVo> data = hasStock.getData("data", new TypeReference<List<SkuStockVo>>() {
                    });
                    if (data != null) {
                        // 将商品ID与库存状态映射，便于在前端展示
                        Map<Long, Boolean> stocks = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                        confirmVo.setStocks(stocks);
                    }
                }, executor);


        // 设置用户积分到订单确认视图对象中
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        try {
            // 等待所有异步任务完成
            CompletableFuture.allOf(getAddressFuture, getCartFuture).get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("获取订单确认信息失败 {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return confirmVo;
    }


}