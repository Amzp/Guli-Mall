/**
 * Copyright 2019 bejson.com
 */
package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * SPU（Standard Product Unit）保存信息的视图对象。
 * 用于封装与SPU相关的基本信息、属性及SKUs等。
 */
@Data
@Accessors(chain = true)
public class SpuSaveVo {

    private String spuName; // SPU名称
    private String spuDescription; // SPU描述
    private Long catalogId; // 所属分类ID
    private Long brandId; // 所属品牌ID
    private BigDecimal weight; // SPU重量
    private int publishStatus; // 发布状态（0未发布，1已发布）
    private List<String> decript; // SPU介绍的图片地址列表
    private List<String> images; // SPU图片列表
    private Bounds bounds; // 促销信息
    private List<BaseAttrs> baseAttrs; // SPU的基础属性列表
    private List<Skus> skus; // SPU下的SKU列表

}
