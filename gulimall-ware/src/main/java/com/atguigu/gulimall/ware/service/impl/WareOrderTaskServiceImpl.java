package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.WareOrderTaskDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("wareOrderTaskService")
public class WareOrderTaskServiceImpl extends ServiceImpl<WareOrderTaskDao, WareOrderTaskEntity> implements WareOrderTaskService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskEntity> page = this.page(
                new Query<WareOrderTaskEntity>().getPage(params),
                new QueryWrapper<WareOrderTaskEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据订单号获取订单任务信息。
     *
     * @param orderSn 订单号，用于查询对应的订单任务信息。
     * @return 返回匹配的订单任务实体，如果没有找到则返回null。
     */
    @Override
    public WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn) {
        // 使用LambdaQueryWrapper构造查询条件，查询订单号为orderSn的订单任务实体
        return this.baseMapper.selectOne(new LambdaQueryWrapper<WareOrderTaskEntity>().eq(WareOrderTaskEntity::getOrderSn, orderSn));
    }
}