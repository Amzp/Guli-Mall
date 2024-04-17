package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * spu信息介绍
 * 
 * @author Rain^
 * @email 843524258@qq.com
 * @date 2019-10-01 21:08:49
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("pms_spu_info_desc")
public class SpuInfoDescEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 商品id
	 * 作为数据库表中的主键
	 */
	@TableId(type = IdType.INPUT)
	private Long spuId;

	/**
	 * 商品介绍
	 * 描述商品的详细信息
	 */
	private String decript;

}
