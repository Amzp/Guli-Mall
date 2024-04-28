package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
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


@Service("orderService")
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    private MemberFeignService memberFeignService;
    @Resource
    private CartFeignService cartFeignService;

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

        // 异步获取用户购物车中的商品列表
        CompletableFuture<Void> getCartFuture = CompletableFuture.runAsync(() -> {
            // 调用购物车服务，获取当前用户购物车中的商品列表
            log.debug("Cart异步线程 id = {}", Thread.currentThread().getId());
            // 恢复当前线程的请求属性，确保调用上下文正确
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 调用远程服务获取购物车商品列表，并设置到订单确认视图对象中
            List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
            confirmVo.setItems(currentCartItems);
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