package com.atguigu.gulimall.product.vo;

import lombok.Data;

@Data
/**
 * AttrRespVo 类继承自 AttrVo，用于响应属性相关信息的VO（值对象）。
 * 除了继承的属性外，增加了所属分类名称、所属分组名称和分类路径。
 */
public class AttrRespVo extends AttrVo {
    // 所属分类名字
    private String catelogName;
    // 所属分组名字
    private String groupName;

    // 分类路径，以长整型数组形式表示，用于表示属性所属的分类层级路径
    private Long[] catelogPath;
}

