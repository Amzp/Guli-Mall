package com.atguigu.gulimall.search.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName: BrandVo
 * Package: com.atguigu.gulimall.search.vo
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/22 下午6:11
 * @Version 1.0
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class BrandVo {
    private Long brandId;
    private String brandName;
}
