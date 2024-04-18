package com.atguigu.gulimall.ware.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDetailDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import org.springframework.util.StringUtils;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    /**
     * 查询采购详情页的分页信息。
     *
     * @param params 包含查询条件的参数映射，可能包含的键有：
     *               - key: 查询关键字，可用于查询purchase_id或sku_id；
     *               - status: 采购单状态；
     *               - wareId: 仓库ID。
     * @return PageUtils 分页工具对象，包含当前页数据和分页信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        // 创建查询包装器用于构建查询条件
        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<PurchaseDetailEntity>();

        // 处理关键字查询条件
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            // 如果有关键字，则添加到查询条件中，可查询purchase_id或sku_id
            queryWrapper.and(w -> {
                w.eq("purchase_id", key).or().eq("sku_id", key);
            });
        }

        // 处理采购单状态查询条件
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            // 如果有状态，则添加到查询条件中
            queryWrapper.eq("status", status);
        }

        // 处理仓库ID查询条件
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            // 如果有仓库ID，则添加到查询条件中
            queryWrapper.eq("ware_id", wareId);
        }

        // 执行分页查询
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        // 返回分页结果
        return new PageUtils(page);
    }


    /**
     * 根据采购单ID列出相应的采购详情实体列表。
     *
     * @param id 采购单的唯一标识符。
     * @return 返回一个包含对应采购单详情的实体列表。
     */
    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        // 使用QueryWrapper构造查询条件，查询purchase_id等于传入ID的所有采购详情实体
        return this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
    }


}