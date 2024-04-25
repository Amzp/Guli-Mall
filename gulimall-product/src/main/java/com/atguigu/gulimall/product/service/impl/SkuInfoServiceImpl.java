package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
@Slf4j
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private AttrGroupService attrGroupService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private ThreadPoolExecutor executor;


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

    /**
     * 查询商品详情信息
     *
     * @param skuId 商品ID，用于唯一标识需要查询的商品
     * @return SkuItemVo 商品详情视图对象，封装了商品的基本信息、销售属性、描述、规格参数、图片等详细信息
     */
    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo(); // 创建商品详情视图对象

        // 异步获取SKU基本信息
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 通过商品ID查询并获取SKU基本信息
            SkuInfoEntity info = this.getById(skuId);
            // 将查询到的SKU基本信息设置到商品详情视图对象中
            skuItemVo.setInfo(info);
            return info; // 返回获取到的SKU基本信息
        }, executor);

        // 异步获取SKU销售属性组合
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(res -> {
            // 根据获取到的SKU基本信息中的SPU ID，查询并获取该SPU的所有销售属性组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            // 将查询到的销售属性组合设置到商品详情视图对象中
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        // 异步获取SPU介绍信息
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            // 根据获取到的SKU基本信息中的SPU ID，查询并获取该SPU的详细介绍信息
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(res.getSpuId());
            // 将查询到的SPU详细介绍信息设置到商品详情视图对象中
            skuItemVo.setDesc(spuInfoDesc);
        }, executor);

        // 异步获取SPU规格参数信息
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            // 根据获取到的SKU基本信息中的SPU ID和Catalog ID，查询并获取该SPU的规格参数信息
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            // 将查询到的SPU规格参数信息设置到商品详情视图对象中
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);

        // 异步获取SKU图片信息
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 通过商品ID查询并获取该SKU的所有图片信息
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            // 将查询到的SKU图片信息设置到商品详情视图对象中
            skuItemVo.setImages(images);
        }, executor);

        // 等待所有异步任务完成
        try {
            // 使用CompletableFuture.allOf()方法等待所有异步任务完成
            CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture).get();
        } catch (InterruptedException | ExecutionException e) {
            // 若在等待过程中发生异常，抛出运行时异常
            log.error("商品详情信息获取失败", e);
            throw new RuntimeException(e);
        }

        return skuItemVo; // 返回填充完毕的商品详情视图对象
    }

}