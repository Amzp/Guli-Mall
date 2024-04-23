package com.atguigu.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuImagesDao;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.service.SkuImagesService;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据SKU ID获取图片列表。
     *
     * @param skuId SKU的唯一标识符。
     * @return 返回一个SKU图片实体列表，这些实体与给定的SKU ID相关联。
     */
    @Override
    public List<SkuImagesEntity> getImagesBySkuId(Long skuId) {

        // 使用LambdaQueryWrapper构造查询条件，查询与给定skuId相等的SkuImagesEntity列表
        return this.baseMapper.selectList(new LambdaQueryWrapper<SkuImagesEntity>().eq(SkuImagesEntity::getSkuId, skuId));
    }

}