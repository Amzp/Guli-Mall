package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.ProductAttrValueDao;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("productAttrValueService")
@Slf4j
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存商品属性值列表
     *
     * @param collect 包含商品属性值的列表，类型为ProductAttrValueEntity的集合。
     *                该方法通过调用saveBatch方法，以批量的方式保存传入的商品属性值集合。
     */
    @Override
    public void saveProductAttr(List<ProductAttrValueEntity> collect) {
        log.info("保存商品属性值列表");
        this.saveBatch(collect); // 执行批量保存操作
    }

    /**
     * 根据SPU ID获取基础属性列表
     *
     * @param spuId 商品规格实体的ID
     * @return 返回对应SPU的基础属性值列表
     */
    @Override
    public List<ProductAttrValueEntity> baseAttrlistforspu(Long spuId) {
        // 使用QueryWrapper构造查询条件，查询SPU_ID等于指定ID的所有属性值
        return this.baseMapper.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
    }


    /**
     * 更新SPU的属性值。首先删除该SPU已有的属性值，然后批量保存新的属性值。
     *
     * @param spuId 商品基本单元ID，用于标识需要更新属性的SPU。
     * @param entities 属性值实体列表，包含了需要更新的属性信息。
     */
    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities) {
        // 删除当前SPU已有的所有属性值
        this.baseMapper.delete(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        // 将传入的属性值实体列表中的SPU ID统一设置为当前要更新的SPU ID，然后批量保存
        List<ProductAttrValueEntity> collect = entities.stream()
                .map(item -> {
                    item.setSpuId(spuId);
                    return item;
                })
                .collect(Collectors.toList());
        this.saveBatch(collect);
    }


}