package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    PurchaseDetailService detailService;

    @Resource
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            //1、新建一个
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        //TODO 确认采购单状态是0,1才可以合并

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();

            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());


        detailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /**
     * 标记采购单为已接收状态。
     * 对传入的采购单ID列表，将相关采购单及其采购项的状态进行更新。
     *
     * @param ids 采购单id列表，表示需要标记为已接收的采购单的ID。
     */
    @Override
    public void received(List<Long> ids) {
        // 确认并更新采购单状态为已接收
        List<PurchaseEntity> collect = ids.stream()
                .map(this::getById) // 通过ID获取采购单实体
                .filter(item ->
                        item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) // 筛选状态为新建或已分配的采购单
                .map(item -> {
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode()); // 更新状态为已接收
                    item.setUpdateTime(new Date()); // 设置更新时间为当前时间
                    return item;
                })
                .collect(Collectors.toList()); // 收集更新后的采购单实体列表

        // 批量更新采购单状态
        this.updateBatchById(collect);

        // 更新采购项状态为购买中
        collect.forEach((item) -> {
            List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId()); // 根据采购单ID获取采购项列表
            List<PurchaseDetailEntity> detailEntities = entities.stream()
                    .map(entity -> PurchaseDetailEntity.builder()
                            .id(entity.getId()) // 采购项ID
                            .status(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode()) // 更新采购项状态为购买中
                            .build()
                    )
                    .collect(Collectors.toList()); // 收集更新后的采购项实体列表
            detailService.updateBatchById(detailEntities); // 批量更新采购项状态
        });
    }


    /**
     * 标记采购单为完成或错误状态，并更新所有采购项的状态。
     * 如果采购项全部成功，则采购单状态更新为完成；如果任一采购项失败，则采购单状态更新为错误。
     * 对于成功的采购项，会进行库存的增加。
     *
     * @param doneVo 包含采购单ID和采购项信息的Vo对象。
     */
    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {

        Long id = doneVo.getId(); // 获取采购单ID

        // 改变采购项的状态
        boolean flag = true; // 标记采购单是否全部成功
        List<PurchaseItemDoneVo> items = doneVo.getItems(); // 获取采购项列表

        List<PurchaseDetailEntity> updates = new ArrayList<>(); // 用于存储需要更新的采购项状态
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false; // 如果有采购项失败，则标记采购单为失败
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                // 对成功采购的物品进行入库操作
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }

        detailService.updateBatchById(updates); // 批量更新采购项状态

        // 改变采购单的状态
        PurchaseEntity purchaseEntity = PurchaseEntity
                .builder()
                .id(id)
                .status(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode())
                .updateTime(new Date())
                .build();
        this.updateById(purchaseEntity); // 更新采购单状态
    }


}