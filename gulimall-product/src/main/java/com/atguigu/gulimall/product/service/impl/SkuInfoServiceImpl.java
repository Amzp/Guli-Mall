package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.SkuImagesService;
import com.atguigu.gulimall.product.service.SpuInfoDescService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("skuInfoService")
@Slf4j
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private AttrGroupService attrGroupService;

    /**
     * 查询SKU信息的分页数据。
     *
     * @param params 查询参数，封装了页码和每页数量等信息。
     * @return 返回分页工具类，包含当前页的数据和分页信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 使用PageHelper进行分页查询，根据传入的参数获取分页信息并构造查询Wrapper
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        // 将查询结果封装成PageUtils返回
        return new PageUtils(page);
    }

    /**
     * 保存SKU信息
     *
     * @param skuInfoEntity SKU信息实体对象，包含SKU的详细信息。
     *                      该方法通过调用baseMapper的insert方法，将传入的SKU信息实体对象插入到数据库中。
     */
    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    /**
     * 根据条件查询SKU信息分页数据
     *
     * @param params 查询条件参数，包括：
     *               key: 搜索关键字
     *               catelogId: 商品目录ID
     *               brandId: 品牌ID
     *               min: 价格下限
     *               max: 价格上限
     * @return 返回分页工具类，包含查询结果信息
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SkuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 根据关键字进行查询，支持SKU ID或SKU名称的模糊匹配
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper
                    .and(wrapper -> {
                        wrapper.eq(SkuInfoEntity::getSkuId, key).or().like(SkuInfoEntity::getSkuName, key);
                    });
        }

        // 根据商品目录ID进行查询，不为"0"时生效
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq(SkuInfoEntity::getCatalogId, catelogId);
        }

        // 根据品牌ID进行查询，不为"0"时生效
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq(SkuInfoEntity::getBrandId, brandId);
        }

        // 根据价格下限进行查询
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge(SkuInfoEntity::getPrice, min);
        }

        // 根据价格上限进行查询，上限大于0时生效
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) > 0) {
                    queryWrapper.le(SkuInfoEntity::getPrice, max);
                }
            } catch (Exception e) {
                log.error("价格区间参数异常{}", max);
            }
        }

        // 执行分页查询
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        // 返回分页结果
        return new PageUtils(page);
    }

    /**
     * 根据SPU ID查询其对应的全部SKU信息以及品牌名称。
     *
     * @param spuId 商品属性组合ID，用于查询相应的SKU信息。
     * @return 返回一个SKU信息实体列表，包含指定SPU ID下的所有SKU信息。
     */
    @Override
    public List<SkuInfoEntity> getSkusBySpuOd(Long spuId) {
        // 使用LambdaQueryWrapper构造查询条件，查询指定spuId的所有sku信息
        return this.list(new LambdaQueryWrapper<SkuInfoEntity>().eq(SkuInfoEntity::getSpuId, spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        // 1. sku基本信息获取
        SkuInfoEntity info = this.getById(skuId);
        skuItemVo.setInfo(info);
        Long catalogId = info.getCatalogId();
        Long spuId = info.getSpuId();

        // 2. sku图片信息
        List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
        skuItemVo.setImages(images);

        // 3. 获取spu的销售属性组合

        // 4. 获取spu的介绍
        SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(spuId);
        skuItemVo.setDesc(spuInfoDesc);


        // 5. 获取spu的规格参数信息
        List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        skuItemVo.setGroupAttrs(attrGroupVos);

        return null;
    }
}