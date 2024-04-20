package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 二级分类Vo
 *
 * @Author Rainbow
 * @Create 2024/4/20 上午9:11
 * @Version 1.0
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Catalog2Vo {
    /**
     * 一级父分类的id
     */
    private String catalog1Id;

    /**
     * 三级子分类列表
     */
    private List<Catalog3Vo> catalog3List;

    /**
     * 二级分类的id
     */
    private String id;

    /**
     * 二级分类的名称
     */
    private String name;

    /**
     * 三级分类vo
     */
    @Data
    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catalog3Vo {
        /**
         * 父分类、二级分类的id
         */
        private String catalog2Id;

        /**
         * 三级分类的id
         */
        private String id;

        /**
         * 三级分类的名称
         */
        private String name;
    }
}
