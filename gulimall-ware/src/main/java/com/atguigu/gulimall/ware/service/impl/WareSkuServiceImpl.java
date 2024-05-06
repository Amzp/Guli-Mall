package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RabbitListener(queues = "stock.release.queue")
@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    WareSkuDao wareSkuDao;

    @Resource
    ProductFeignService productFeignService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private WareOrderTaskService wareOrderTaskService;

    @Resource
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Resource
    private OrderFeignService orderFeignService;


    /**
     * 尝试为订单锁定所需库存。此方法遍历订单中的每个商品项，查找有库存的仓库，
     * 并尝试锁定库存。如果所有商品的库存均成功锁定，则返回true；过程中若遇到任何商品无法锁定库存，
     * 则通过抛出NoStockException异常来中止操作。
     *
     * @param vo {@link WareSkuLockVo} 订单锁定库存请求参数，包含：
     *           - orderSn：订单号，用于创建库存工作单的追踪标识。
     *           - locks：商品锁定信息列表，每个元素包含商品ID和需锁定的数量。
     * @return boolean 如果所有商品的库存锁定操作均成功，则返回true。
     * @throws NoStockException 如果发现没有仓库能为某个商品提供足够的库存进行锁定时抛出此异常。
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        // 初始化并保存一个库存工作单实体，记录订单锁定库存的操作日志
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn()); // 设置订单号
        wareOrderTaskEntity.setCreateTime(new Date()); // 记录创建时间
        wareOrderTaskService.save(wareOrderTaskEntity); // 保存到数据库

        // 获取订单中所有需要锁定库存的商品项
        List<OrderItemVo> locks = vo.getLocks();

        // 遍历订单中的每个商品项，构造SkuWareHasStock对象列表，用于后续锁定库存操作
        // 每个对象包含商品ID、需要锁定的数量以及该商品有库存的仓库ID列表
        List<SkuWareHasStock> collect = locks.stream()
                .map(item -> {
                    SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
                    Long skuId = item.getSkuId(); // 商品ID
                    skuWareHasStock.setSkuId(skuId);
                    skuWareHasStock.setNum(item.getCount()); // 需要锁定的数量
                    // 查询该商品在哪些仓库有库存
                    List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
                    skuWareHasStock.setWareId(wareIds); // 设置有库存的仓库ID列表
                    return skuWareHasStock;
                })
                .collect(Collectors.toList());

        // 对于每个商品，尝试从其有库存的仓库中锁定库存
        for (SkuWareHasStock hasStock : collect) {
            boolean skuStocked = false; // 标记当前商品是否已成功锁定库存
            Long skuId = hasStock.getSkuId(); // 当前处理的商品ID
            List<Long> wareIds = hasStock.getWareId(); // 可能有库存的仓库ID列表

            // 检查是否有仓库可尝试锁定库存
            if (wareIds.isEmpty()) {
                log.info("没有找到有库存的仓库，商品id:{}", skuId);
                throw new NoStockException(skuId); // 无库存可锁定，抛出异常
            }

            // 遍历仓库，尝试锁定库存
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum()); // 尝试锁定库存
                if (count == 1) { // 锁定成功
                    skuStocked = true;
                    // 记录锁定成功的库存详情
                    WareOrderTaskDetailEntity taskDetailEntity = WareOrderTaskDetailEntity.builder()
                            .skuId(skuId)
                            .skuName("") // 注意：此处skuName未赋值，实际使用时可能需要填充
                            .skuNum(hasStock.getNum())
                            .taskId(wareOrderTaskEntity.getId())
                            .wareId(wareId)
                            .lockStatus(1) // 锁定状态：1表示锁定成功
                            .build();
                    wareOrderTaskDetailService.save(taskDetailEntity); // 保存锁定详情至数据库

                    // TODO: 发送一条消息到消息队列（MQ），通知系统其他部分库存锁定已完成。
                    // 创建库存锁定目标对象
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(wareOrderTaskEntity.getId());

                    // 复制任务详细信息到库存详细信息对象
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity, detailTo);
                    lockedTo.setDetailTo(detailTo);

                    // 将库存锁定信息发送到RabbitMQ队列
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);

                    break; // 成功锁定后，跳出循环，避免重复锁定
                }
                // 如果当前仓库锁定失败，将继续尝试下一个仓库（逻辑上这里应有else部分以处理循环，但根据原始代码逻辑已隐含）
            }

            // 检查是否成功锁定当前商品的库存
            if (!skuStocked) {
                // 所有尝试的仓库都无法锁定该商品的库存
                log.info("没有找到有库存的仓库，商品id:{}", skuId);
                throw new NoStockException(skuId); // 抛出异常，终止订单处理
            }
        }
        // 执行到此，说明所有商品的库存锁定均成功
        return true;
    }


    @Data
    class SkuWareHasStock {
        private Long skuId;
        private List<Long> wareId;
        private Integer num;
    }

    /**
     * 根据传入的参数查询指定的SKU库存信息分页列表。
     *
     * @param params 包含查询参数的Map，其中可以包含skuId和wareId。
     * @return 返回查询结果的分页工具类PageUtils，包含当前页的数据和分页信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        // 根据传入的skuId参数构建查询条件
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        // 根据传入的wareId参数构建查询条件
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        // 执行分页查询
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        // 封装查询结果为PageUtils对象并返回
        return new PageUtils(page);
    }


    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.isEmpty()) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                log.error("远程查询sku信息失败");
            }
            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    /**
     * 查询指定SKU是否具有库存
     *
     * @param skuIds SKU编号列表，需要查询其库存状况
     * @return 返回一个SkuHasStockVo对象列表，其中包含了指定SKU是否具有库存的信息
     */
    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        // 对传入的SKU编号列表进行流式处理
        return skuIds.stream()
                .map(skuId -> {
                    // 根据SKU编号查询该SKU的库存量
                    Long count = this.baseMapper.getSkuStock(skuId);
                    // 构建并返回一个包含SKU编号和是否有库存信息的对象
                    return SkuHasStockVo.builder()
                            .skuId(skuId)
                            .hasStock(count != null && count > 0).build();
                })
                // 集合化处理，将所有SKU的库存查询结果收集到一个列表中
                .collect(Collectors.toList());
    }


    /**
     * 解锁库存。
     * 该方法用于根据指定的库存锁定信息解锁库存。首先，通过库存锁定详情的ID查询相关的锁定信息，然后根据订单的状态来决定是否进行库存的解锁操作。
     * 如果订单状态为已取消或者订单不存在，且库存锁定状态为已锁定但未解锁，将执行库存解锁流程。
     *
     * @param to 包含库存锁定详情的实体对象。
     */
    @Override
    public void unlockStock(StockLockedTo to) {
        // 获取库存锁定的详细信息
        StockDetailTo detail = to.getDetailTo();
        Long detailId = detail.getId();

        // 执行解锁逻辑
        WareOrderTaskDetailEntity taskDetailInfo = wareOrderTaskDetailService.getById(detailId);
        if (taskDetailInfo != null) {
            // 获取相关的工作单信息
            Long id = to.getId();
            WareOrderTaskEntity orderTaskInfo = wareOrderTaskService.getById(id);
            // 通过订单号查询订单状态
            String orderSn = orderTaskInfo.getOrderSn();
            // 远程调用查询订单状态
            R orderData = orderFeignService.getOrderStatus(orderSn);
            if (orderData.getCode() == 0) {
                // 订单状态查询成功，进行订单状态判断
                OrderVo orderInfo = orderData.getData("data", new TypeReference<OrderVo>() {
                });

                // 判断是否需要解锁库存
                if (orderInfo == null || orderInfo.getStatus() == 4) {
                    // 订单被取消或不存在，且库存锁定状态为已锁定，执行解锁
                    if (taskDetailInfo.getLockStatus() == 1) {
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                // 订单状态查询失败，抛出异常
                throw new RuntimeException("远程调用服务失败");
            }
        } else {
            // 无需解锁库存，直接退出
        }
    }


    /**
     * 解锁订单关联的库存，确保订单服务异常不会导致库存永久锁定。
     * 该方法首先检查库存解锁状态，避免重复解锁操作，然后根据工作单ID解锁所有未解锁的库存项。
     *
     * @param orderTo 订单信息对象，包含订单编号等信息。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unlockStock(OrderTo orderTo) {

        String orderSn = orderTo.getOrderSn();
        // 根据订单编号查询最新的库存解锁任务状态，防止重复执行解锁操作
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);

        // 获取当前任务ID，用于查询需要解锁的库存详细信息
        Long id = orderTaskEntity.getId();
        // 查询所有该任务下未解锁（lock_status为1）的库存详细信息
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id).eq("lock_status", 1));

        // 遍历未解锁的库存项，逐个执行解锁操作
        for (WareOrderTaskDetailEntity taskDetailEntity : list) {
            unLockStock(taskDetailEntity.getSkuId(),
                    taskDetailEntity.getWareId(),
                    taskDetailEntity.getSkuNum(),
                    taskDetailEntity.getId());
        }
    }


    /**
     * 解锁库存的方法。此方法用于在完成特定操作（如出库）后，将之前锁定的库存解锁，使其可以被其他操作所使用。
     * 同时，该方法也会更新相关的工作单状态，标记该部分库存已解锁。
     *
     * @param skuId 商品ID，用于指定需要解锁的库存商品。
     * @param wareId 仓库ID，指定库存所在的仓库。
     * @param num 解锁的数量，指定需要解锁的库存商品的数量。
     * @param taskDetailId 工作单详情ID，用于标识是哪个工作单的哪个详情项需要进行解锁操作。
     */
    public void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {

        // 解锁指定商品在指定仓库中的库存
        wareSkuDao.unLockStock(skuId, wareId, num);

        // 更新相关工作单的状态为已解锁
        WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
        taskDetailEntity.setId(taskDetailId);
        // 设置解锁状态
        taskDetailEntity.setLockStatus(2);
        // 执行更新操作
        wareOrderTaskDetailService.updateById(taskDetailEntity);

    }


}