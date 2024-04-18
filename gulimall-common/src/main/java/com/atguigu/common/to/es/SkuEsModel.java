package com.atguigu.common.to.es;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: SkuEsModel
 * Package: com.atguigu.common.to.es
 * Description: SKU的ES模型类，用于将SKU信息映射到ES索引中。该类包含了SKU的基本信息、销售信息、品牌信息、分类信息以及属性信息。
 *
 * @Author Rainbow
 * @Create 2024/4/18 下午10:20
 * @Version 1.0
 */

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SkuEsModel {
    private Long skuId; // SKU编号
    private Long spuId; // SPU编号
    private String skuTitle; // SKU标题
    private BigDecimal skuPrice; // SKU价格
    private String skuImg; // SKU图片地址
    private Long saleCount; // 销售数量
    private Boolean hasStock; // 是否有库存
    private Long hotScore; // 热度评分
    private Long brandId; // 品牌编号
    private Long catalogId; // 分类编号
    private String brandName; // 品牌名称
    private String brandImg; // 品牌图片地址
    private String catalogName; // 分类名称
    private List<Attrs> attrs; // SKU属性集合

    /**
     * SKU属性类，用于描述SKU的属性信息，如颜色、尺码等。
     */
    @Data
    @Builder
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attrs {
        private Long attrId; // 属性编号
        private String attrName; // 属性名称
        private String attrValue; // 属性值
    }
}
