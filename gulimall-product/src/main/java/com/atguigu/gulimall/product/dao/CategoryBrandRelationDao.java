package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 品牌分类关联
 * 
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-11-17 21:25:25
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    /**
     * 更新分类信息。
     *
     * @param catId 分类的唯一标识符。
     * @param name 分类的新名称。
     * 该方法不返回任何内容。
     */
    void updateCategory(@Param("catId") Long catId, @Param("name") String name);

}
