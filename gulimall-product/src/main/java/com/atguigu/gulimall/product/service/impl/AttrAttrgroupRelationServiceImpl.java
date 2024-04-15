package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    /**
     * 查询属性和属性组关系的分页数据
     *
     * @param params 查询参数，包含分页信息和过滤条件
     * @return 返回分页查询结果的工具类，包含当前页数据、总页数、总记录数等信息
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 使用PageHelper进行分页查询，根据传入的参数构建分页信息和查询条件
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        // 封装分页查询结果到PageUtils工具类中并返回
        return new PageUtils(page);
    }

    /**
     * 批量保存属性组关系。
     *
     * @param vos 属性组关系视图对象列表，类型为List<AttrGroupRelationVo>。这个参数包含了需要被批量保存的属性组关系信息。
     *
     * 函数首先将传入的视图对象列表（vos）映射转换成实体对象列表（collect），
     * 然后调用另一个saveBatch方法保存这些实体对象。
     */
    @Override
    public void saveBatch(List<AttrGroupRelationVo> vos) {
        // 将视图对象列表转换为实体对象列表，并进行批量保存
        List<AttrAttrgroupRelationEntity> collect = vos.stream()
                .map(item -> {
                    AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                    // 使用BeanUtils工具类复制属性，从视图对象到实体对象
                    BeanUtils.copyProperties(item, relationEntity);
                    return relationEntity;
                })
                .collect(Collectors.toList());
        // 执行批量保存操作
        this.saveBatch(collect);
    }

}