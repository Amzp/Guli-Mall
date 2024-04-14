package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;


/**
 * 商品三级分类
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 21:08:48
 */

@Data
@TableName("pms_category")
public class CategoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分类id
     */
    @TableId
    private Long catId;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 父分类id
     */
    private Long parentCid;
    /**
     * 层级
     */
    private Integer catLevel;
    /**
     * 是否显示[0-不显示，1-显示]
     * 通过@TableLogic注解，实现逻辑删除的功能。当showStatus的值为1时，表示数据正常展示；为0时，表示数据被逻辑删除。
     */
    @TableLogic(value = "1", delval = "0")
    private Integer showStatus;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 图标地址
     */
    private String icon;
    /**
     * 计量单位
     */
    private String productUnit;
    /**
     * 商品数量
     */
    private Integer productCount;
    /**
     * 子分类
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)  // 用于控制序列化时是否包含该字段，若为null则不序列化该字段。
    @TableField(exist = false)    // 用于实体类的字段上，用于告诉 MyBatis-Plus 在进行数据库操作时忽略这个字段，因为在数据库表中并不存在对应的字段。
    private List<CategoryEntity> children;


}
