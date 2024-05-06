package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.OrderTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.to.SpuInfoVo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lly835.bestpay.service.BestPayService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    private ProductFeignService productFeignService;

    @Resource
    private OrderItemService orderItemService;
    @Resource
    private PaymentInfoService paymentInfoService;
//    @Resource
//    private BestPayService bestPayService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ThreadPoolExecutor executor;

    /**
     * 创建一个ThreadLocal变量，用于存储订单确认信息。
     * ThreadLocal为每个线程提供了一个独立的变量副本，确保了不同线程之间变量的独立性。
     * 在该类中，ThreadLocal被用来存储OrderSubmitVo对象，方便在线程执行过程中访问订单确认信息。
     */
    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<>()
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

        /**
         * 生成并设置防重令牌
         * 该操作会为指定用户生成一个唯一的防重令牌，并将其存储到Redis中，以供后续请求验证。
         * 令牌有效期为30分钟。
         *
         * @param memberRespVo 用户信息响应对象，包含用户的唯一标识。
         * @param confirmVo 确认订单的信息对象，将设置生成的令牌。
         */
        // 生成一个不含横线的UUID作为防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        // 将生成的令牌以用户ID为键存储到Redis中，设置过期时间为30分钟
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        // 将生成的令牌设置到确认订单信息对象中
        confirmVo.setOrderToken(token);

        try {
            // 等待所有异步任务完成
            CompletableFuture.allOf(getAddressFuture, getCartFuture).get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("获取订单确认信息失败 {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return confirmVo;
    }

    /**
     * 提交订单
     *
     * @param vo
     * @return
     */
//    @GlobalTransactional
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        // 获取当前登录用户的成员响应实体
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        // 创建一个提交订单响应实体
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        // 将给定的Vo实体设置到线程本地存储中
        submitVoThreadLocal.set(vo);
        // 设置响应状态为成功
        responseVo.setCode(0);

        // 1. 验证令牌：令牌的对比和删除必须保证原子性
        // 构造一个Redis脚本，用于检查指定键的值是否与给定的值相等，如果相等，则删除该键。
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // 获取订单令牌
        String orderToken = vo.getOrderToken();
        // 执行Redis脚本，传入键名和要比较的值，返回结果为删除成功与否的标志（1为成功，0为失败）
        Long result = stringRedisTemplate.execute(
                // 1. 实例化一个DefaultRedisScript对象
                new DefaultRedisScript<>(script, Long.class),
                // 2. 准备Redis Key
                Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),
                // 3. 提供给脚本的参数
                orderToken);


        if (result != null && result == 0L) {
            // 令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            // 1. 创建订单
            OrderCreateTo order = createOrder();
            // 2. 检验价格
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 3. 保存订单
                saveOrder(order);
                // 4. 库存锁定
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream()
                        .map(item -> new OrderItemVo()
                                .setSkuId(item.getSkuId())
                                .setCount(item.getSkuQuantity())
                                .setTitle(item.getSkuName()))
                        .collect(Collectors.toList());
                wareSkuLockVo.setLocks(locks);

                // 为了保证高并发，让库存服务自己回滚，发消息给库存服务
                // 库存服务本身也可以使用自动解锁模式
                //调用远程锁定库存的方法
                //出现的问题：扣减库存成功了，但是由于网络原因超时，出现异常，导致订单事务回滚，库存事务不回滚(解决方案：seata)
                //为了保证高并发，不推荐使用seata，因为是加锁，并行化，提升不了效率,可以发消息给库存服务
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    // 锁定成功
                    responseVo.setOrder(order.getOrder());

                    // 人为制造一个异常
//                    int i = 10 / 0;

                    // 5. 发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    log.info("订单创建成功，订单号：{}", order.getOrder().getOrderSn());
                    //删除购物车里的数据
                    stringRedisTemplate.delete(CartConstant.CART_PREFIX + memberRespVo.getId());
                    return responseVo;
                } else {
                    // 锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }
            } else {
                // 验证失败
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    /**
     * 保存订单及其订单项信息。
     *
     * @param orderCreateTo 包含订单信息和订单项信息的传输对象。
     *                      其中，订单信息包括：订单各项属性；
     *                      订单项信息包括：订单项的各项属性。
     *                      该方法将订单信息和订单项信息分别保存到数据库中。
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {
        // 获取并设置订单基本信息
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        order.setCreateTime(new Date());

        // 保存订单信息到数据库
        this.baseMapper.insert(order);

        // 获取订单项信息并保存
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();

        // 批量保存订单项信息到数据库
        orderItemService.saveBatch(orderItems);
    }


    /**
     * 创建订单
     * <p>
     * 本方法负责创建订单及其项的信息，主要包括以下步骤：
     * 1. 生成订单编号；
     * 2. 构建订单实体；
     * 3. 获取订单项实体列表；
     * 4. 校验并计算订单总价；
     * 5. 将订单实体和订单项实体封装到OrderCreateTo对象中并返回。
     *
     * @return OrderCreateTo 包含订单及其项信息的对象
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1. 生成订单号
        String orderSn = IdWorker.getTimeId();

        OrderEntity orderEntity = buildOrder(orderSn);

        // 2. 获取订单项信息
        List<OrderItemEntity> items = buildOrderItems(orderSn);

        // 3. 检验价格
        if (items != null) {
            computePrice(orderEntity, items);
        }

        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(items);

        return orderCreateTo;
    }


    /**
     * 计算订单价格及优惠信息。
     *
     * @param orderEntity       订单实体，用于存储订单的总金额、优惠金额等信息。
     * @param orderItemEntities 订单项实体列表，包含订单中的各个商品及其详细信息，用于计算订单总额及各项优惠。
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        // 初始化订单总额、各项优惠金额以及积分、成长值
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        // 初始化积分总数和成长值总数
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        // 遍历订单项，累加订单总额及各项优惠金额，并计算积分和成长值
        for (OrderItemEntity orderItem : orderItemEntities) {
            // 累加优惠价格信息
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            intergration = intergration.add(orderItem.getIntegrationAmount());

            // 累加总价
            total = total.add(orderItem.getRealAmount());

            // 累加积分和成长值
            integrationTotal += orderItem.getGiftIntegration();
            growthTotal += orderItem.getGiftGrowth();
        }

        // 设置订单价格相关的信息
        orderEntity.setTotalAmount(total);
        // 计算应付总额，即总额加上运费
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        // 设置积分和成长值信息
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        // 设置订单的删除状态为未删除
        orderEntity.setDeleteStatus(0);
    }


    /**
     * 构建订单实体。
     * 该方法根据给定的订单编号和收货地址信息（包括运费），使用建造者模式组装一个订单实体。
     *
     * @param orderSn 订单编号，用于标识唯一的订单。
     * @return OrderEntity 订单实体，包含了订单的基本信息和运费等。
     */
    private OrderEntity buildOrder(String orderSn) {
        // 从线程本地存储获取会员信息
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        // 从线程本地存储获取订单提交信息
        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        // 调用远程服务获取运费信息
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        // 将运费信息反序列化为FareVo对象
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });

        // 使用建造者模式初始化OrderEntity并设置运费及收货人信息
        return OrderEntity.builder()
                .memberId(memberRespVo.getId()) // 初始化会员ID
                .orderSn(orderSn) // 设置订单编号
                .freightAmount(fareResp.getFare()) // 初始化运费金额
                .receiverCity(fareResp.getAddress().getCity()) // 初始化收货人所在城市
                .receiverDetailAddress(fareResp.getAddress().getDetailAddress()) // 初始化收货人的详细地址
                .receiverName(fareResp.getAddress().getName()) // 初始化收货人姓名
                .receiverPhone(fareResp.getAddress().getPhone()) // 初始化收货人电话
                .receiverPostCode(fareResp.getAddress().getPostCode()) // 初始化收货人邮政编码
                .receiverProvince(fareResp.getAddress().getProvince()) // 初始化收货人所在省份
                .receiverRegion(fareResp.getAddress().getRegion()) // 初始化收货人所在区域
                .status(OrderStatusEnum.CREATE_NEW.getCode()) // 初始化订单状态为待付款
                .autoConfirmDay(7)  // 设置自动确认收货时间
                .confirmStatus(0)   // 初始化订单确认状态为未确认
                .build();
    }

    /**
     * 构建所有订单项数据。
     * 该方法通过调用cartFeignService来获取当前购物车中的商品项，并将这些商品项转换为订单项实体列表。
     * 每个订单项实体会设置订单编号（orderSn）。
     *
     * @param orderSn 订单编号，用于设置每个订单项实体。
     * @return 返回包含所有订单项实体的列表。如果当前购物车为空，则返回null。
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // 获取当前购物车中的商品项
        List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
        if (currentCartItems != null && !currentCartItems.isEmpty()) {
            // 将购物车项转换为订单项实体，并设置订单编号
            return currentCartItems.stream()
                    .map(cartItem -> {
                        OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                        orderItemEntity.setOrderSn(orderSn);
                        return orderItemEntity;
                    })
                    .collect(Collectors.toList());
        } else {
            // 如果购物车为空，返回null
            return null;
        }

    }


    /**
     * 构建单个订单项数据
     *
     * @param cartItem 购物车项信息，包含商品的SKU信息、数量、属性等
     * @return 返回构建完成的订单项实体对象，包含商品的详细信息、优惠信息、价格信息等
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        // 构建商品的SPU信息
        Long skuId = cartItem.getSkuId();
        // 通过SKU ID获取SPU的信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoData = spuInfo.getData("data", new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoData.getId())
                .setSpuName(spuInfoData.getSpuName())
                .setSpuBrand(spuInfoData.getBrandId().toString())
                .setCategoryId(spuInfoData.getCatalogId());

        // 构建商品的SKU信息
        orderItemEntity.setSkuId(skuId)
                .setSkuName(cartItem.getTitle())
                .setSkuPic(cartItem.getImage())
                .setSkuPrice(cartItem.getPrice())
                .setSkuQuantity(cartItem.getCount());

        // 将SKU属性值列表转换为字符串
        String skuAttrValues = StringUtils.collectionToDelimitedString(cartItem.getSkuAttrValues(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrValues);

        // 构建商品的优惠信息（待实现）

        // 构建商品的积分信息
        orderItemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue())
                .setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());

        // 构建订单项的价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO)
                .setCouponAmount(BigDecimal.ZERO)
                .setIntegrationAmount(BigDecimal.ZERO);

        // 计算订单项的实际金额
        // 先计算原价，然后减去各种优惠金额，得到实际支付金额
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = origin
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }


    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderSn, orderSn));
    }


    /**
     * 关闭订单。
     * 在关闭订单之前，首先检查订单的状态是否为待支付（即是否已支付）。如果订单状态为待支付，则将订单状态更新为已取消，并发送一条消息到消息队列。
     *
     * @param orderEntity 包含订单信息的实体对象。此对象应包含订单的唯一标识，如订单号（orderSn）。
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        // 根据订单号查询订单状态
        OrderEntity orderInfo = this.getOne(new QueryWrapper<OrderEntity>().
                eq("order_sn", orderEntity.getOrderSn()));

        // 检查订单状态是否为待支付，若是则进行关闭订单操作
        if (orderInfo.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            // 更新订单状态为已取消
            OrderEntity orderUpdate = new OrderEntity();
            orderUpdate.setId(orderInfo.getId());
            orderUpdate.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(orderUpdate);

            // 准备订单消息，发送到消息队列
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderInfo, orderTo);

            try {
                // 发送订单取消消息到消息队列
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                // 捕获发送消息异常，计划实现：定期扫描数据库，重新发送失败的消息
            }
        }
    }
}