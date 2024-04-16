package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * SpuInfoEntity 类表示一个商品信息实体，用于存储商品的详细信息。
 *
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 21:08:49
 */

@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pms_spu_info")
public class SpuInfoEntity implements Serializable {
    /**
     * 序列化ID，用于版本控制。该字段为静态常量，值为1L。
     * 在Java中，如果一个类实现了Serializable接口，那么就可以将该类的对象序列化。
     * 序列化ID的作用是在反序列化时，确保反序列化的对象和序列化的对象是同一个版本。
     */
    private static final long serialVersionUID = 1L;

    /**
     * 商品id，作为主键标识一个商品。
     */
    @TableId
    private Long id;
    /**
     * 商品名称，用于描述商品的名称。
     */
    private String spuName;
    /**
     * 商品描述，提供商品的详细描述信息。
     */
    private String spuDescription;
    /**
     * 所属分类id，指示该商品属于哪个分类。
     */
    private Long catalogId;
    /**
     * 品牌id，表示该商品所属的品牌。
     */
    private Long brandId;
    /**
     * 商品重量，用于表示商品的重量信息。
     */
    private BigDecimal weight;
    /**
     * 上架状态，用于控制商品的上架状态。[0 - 新建，1 - 上架，2-下架]
     */
    private Integer publishStatus;
    /**
     * 创建时间，记录商品信息的创建时间。
     */
    private Date createTime;
    /**
     * 更新时间，记录商品信息的最后更新时间。
     */
    private Date updateTime;

}
