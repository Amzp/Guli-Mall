package com.atguigu.gulimall.ware.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName: SkuHasStockVo
 * Package: com.atguigu.gulimall.ware.vo
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/18 下午11:27
 * @Version 1.0
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SkuHasStockVo {
    private Long skuId;
    private Boolean hasStock;
}
