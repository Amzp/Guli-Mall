package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * SKU商品信息模型类
 * 用于封装SKU的详细信息，包括基本属性、图片、销售属性、描述及规格参数等。
 */
@ToString
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SkuItemVo {

    // sku基本信息
    private SkuInfoEntity info;

    // 标记该sku是否有库存，默认为true
    private boolean hasStock = true;

    // sku的图片信息集合
    private List<SkuImagesEntity> images;

    // spu的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttr;

    // spu的介绍信息
    private SpuInfoDescEntity desc;

    // spu的规格参数信息集合
    private List<SpuItemAttrGroupVo> groupAttrs;

    // 秒杀商品的优惠信息
    private SeckillSkuVo seckillSkuVo;

}
