package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Catalog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 21:08:48
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();


    /**
     * 根据提供的ID列表移除菜单项。
     *
     * @param asList 包含需要移除的菜单项ID的列表。列表中的每个元素都应为Long类型。
     *               该参数不应为null，且至少应包含一个元素。
     * @return 无返回值。
     */
    void removeMenuByIds(List<Long> asList);

    /**
     * 找到catelogId的完整路径；
     * [父/子/孙]
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, List<Catalog2Vo>> getCatalogJson();
}

