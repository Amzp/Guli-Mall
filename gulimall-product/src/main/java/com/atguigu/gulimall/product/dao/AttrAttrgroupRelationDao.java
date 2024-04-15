package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性与属性组关联的映射接口
 * 该接口用于定义属性和属性组之间的关联操作，继承自BaseMapper，提供了基本的CRUD操作方法。
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 21:08:49
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    /**
     * 批量删除属性与属性组的关联关系
     * 该方法用于根据提供的属性组关联实体列表，批量从数据库中删除相应的关联记录。
     *
     * @param entities 属性组关联实体列表，包含需要删除的关联关系。
     */
    void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);

}
