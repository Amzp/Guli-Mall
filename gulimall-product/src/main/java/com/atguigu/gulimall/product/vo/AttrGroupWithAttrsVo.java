package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 属性分组及其属性值对象类
 * 用于封装属性分组的相关信息和该分组下的属性实体列表
 */
@Data
@Accessors(chain = true)
public class AttrGroupWithAttrsVo {

    /**
     * 分组id
     * 用于唯一标识一个属性分组
     */
    private Long attrGroupId;

    /**
     * 组名
     * 描述属性分组的名称
     */
    private String attrGroupName;

    /**
     * 排序
     * 用于指定属性分组内部属性的排序规则
     */
    private Integer sort;

    /**
     * 描述
     * 提供属性分组的详细描述信息
     */
    private String descript;

    /**
     * 组图标
     * 代表属性分组的图标链接
     */
    private String icon;

    /**
     * 所属分类id
     * 指定属性分组所属的分类的唯一标识
     */
    private Long catelogId;

    /**
     * 属性列表
     * 包含属于该属性分组的所有属性实体
     */
    private List<AttrEntity> attrs;
}
