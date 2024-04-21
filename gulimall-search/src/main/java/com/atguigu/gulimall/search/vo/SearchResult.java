package com.atguigu.gulimall.search.vo;

import com.atguigu.common.es.SkuEsModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    /**
     * 搜索结果类，封装了搜索到的商品信息、分页信息及导航信息
     */

    // 查询到的所有商品信息
    private List<SkuEsModel> product;

    // 当前页码
    private Integer pageNum;

    // 总记录数
    private Long total;

    // 总页码
    private Integer totalPages;

    // 分页导航链接列表
    private List<Integer> pageNavs;

    // 当前查询到的结果，涉及到的所有品牌
    private List<BrandVo> brands;

    // 当前查询到的结果，涉及到的所有属性
    private List<AttrVo> attrs;

    // 当前查询到的结果，涉及到的所有分类
    private List<CatalogVo> catalogs;

    //===========================以上是返回给页面的所有信息============================//

    // 面包屑导航数据
    private List<NavVo> navs;

    /**
     * NavVo 类封装了面包屑导航的名称、值和链接
     */
    @Data
    public static class NavVo {
        private String navName; // 导航名称
        private String navValue; // 导航值
        private String link; // 导航链接
    }

    /**
     * BrandVo 类封装了品牌的信息，包括品牌ID、名称和图片链接
     */
    @Data
    public static class BrandVo {
        private Long brandId; // 品牌ID
        private String brandName; // 品牌名称
        private String brandImg; // 品牌图片链接
    }

    /**
     * AttrVo 类封装了商品属性的信息，包括属性ID、名称和值列表
     */
    @Data
    public static class AttrVo {
        private Long attrId; // 属性ID
        private String attrName; // 属性名称
        private List<String> attrValue; // 属性值列表
    }

    /**
     * CatalogVo 类封装了商品分类的信息，包括分类ID和名称
     */
    @Data
    public static class CatalogVo {
        private Long catalogId; // 分类ID
        private String catalogName; // 分类名称
    }
}
