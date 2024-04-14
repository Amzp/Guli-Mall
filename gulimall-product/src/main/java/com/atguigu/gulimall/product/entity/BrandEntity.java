package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 21:08:49
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     * 此字段用于标识品牌的身份，在数据库中作为主键使用。
     * 它通过不同的验证组来区分新增和更新操作：
     * <p>- 在更新操作（UpdateGroup）中，品牌id不能为空，必须指定品牌id进行更新；</p>
     * <p>- 在新增操作（AddGroup）中，品牌id不能指定，因为新增的品牌应该分配新的id。</p>
     */
    @NotNull(message = "修改必须指定品牌id", groups = {UpdateGroup.class})
    @Null(message = "新增不能指定id", groups = {AddGroup.class})
    @TableId
    private Long brandId;

    /**
     * 品牌名字段。
     * <p>此字段必须填写，且不能为null或空字符串，否则将触发验证错误。</p>
     *
     * @param name 品牌的名称。必须非空。
     * @see AddGroup 该字段参与{@link AddGroup}验证组。
     * @see UpdateGroup 该字段参与{@link UpdateGroup}验证组。
     */
    @NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
    private String name;

    /**
     * 品牌logo地址
     * 此字段用于指定品牌logo的网络地址。
     * 它必须是一个非空的合法URL地址，且在添加（AddGroup）和更新（UpdateGroup）品牌信息时都需要验证其合法性。
     */
    @NotBlank(groups = {AddGroup.class})
    @URL(message = "logo必须是一个合法的url地址", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;

    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
//	@Pattern()
    @NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
    @ListValue(vals = {0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(groups = {AddGroup.class})
    @Min(value = 0, message = "排序必须大于等于0", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;

}
