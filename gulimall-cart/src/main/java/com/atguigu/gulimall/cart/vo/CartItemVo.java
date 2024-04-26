package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 购物项内容
 * @Created: with IntelliJ IDEA.
 * @author: Rain^
 * @createTime: 2020-06-30 16:43
 **/

@Data
public class CartItemVo {

    // SKU唯一标识
    private Long skuId;

    // 商品检查标志，默认为true
    private Boolean check = true;

    // 商品标题
    private String title;

    // 商品图片链接
    private String image;

    /**
     * 商品套餐属性值列表
     */
    private List<String> skuAttrValues;

    // 商品单价
    private BigDecimal price;

    // 商品数量
    private Integer count;

    // 商品小计（单价 * 数量）
    private BigDecimal totalPrice;


    /**
     * 计算当前购物项的总价。
     *
     * @return BigDecimal 返回当前购物项的总价。
     */
    public BigDecimal getTotalPrice() {
        // 计算并返回购物项的总价
        return this.price.multiply(new BigDecimal("" + this.count));
    }


}
