package com.atguigu.gulimall.product.vo;

import lombok.Data;

@Data
/**
 * 属性组关系视图对象类
 * 用于表示属性(id)与属性组(id)之间的关系
 */
public class AttrGroupRelationVo {

    // 属性ID
    private Long attrId;
    // 属性组ID
    private Long attrGroupId;
}
