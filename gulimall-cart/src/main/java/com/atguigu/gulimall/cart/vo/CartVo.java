package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 整个购物车存放的商品信息   需要计算的属性需要重写get方法，保证每次获取属性都会进行计算
 * @Created: with IntelliJ IDEA.
 * @author: Rain^
 * @createTime: 2020-06-30 16:42
 **/
@Data
public class CartVo {

    /**
     * 购物车子项信息
     */
    List<CartItemVo> items;

    /**
     * 商品数量
     */
    private Integer countNum;

    /**
     * 商品类型数量
     */
    private Integer countType;

    /**
     * 商品总价
     */
    private BigDecimal totalAmount;

    /**
     * 减免价格
     */
    private BigDecimal reduce = new BigDecimal("0.00");


    /**
     * 获取购物车中所有商品的数量总和。
     * <p>此方法不接受任何参数，它会遍历购物车中的每件商品，并将它们的数量相加，最后返回总和。</p>
     *
     * @return 返回购物车中所有商品数量的总和。如果购物车为空或没有商品，则返回0。
     */
    public Integer getCountNum() {
        int count = 0; // 初始化商品数量总和为0
        // 检查购物车物品列表是否非空且不为空列表
        if (items != null && !items.isEmpty()) {
            // 遍历购物车中的每件商品，累加其数量
            for (CartItemVo item : items) {
                count += item.getCount();
            }
        }
        return count; // 返回商品数量总和
    }

    /**
     * 获取购物车中商品的数量。
     * <p>此方法不接受参数，它会遍历购物车中的所有商品项（如果购物车不为空），
     * 并计算其总数，然后返回。</p>
     *
     * @return 返回购物车中商品的数量。如果购物车为空，则返回0。
     */
    public Integer getCountType() {
        // 初始化商品数量为0
        int count = 0;
        // 检查购物车是否已经初始化且不为空
        if (items != null && !items.isEmpty()) {
            // 遍历购物车中的每一个商品项，累加数量
            for (CartItemVo item : items) {
                count += 1;
            }
        }
        // 返回购物车中商品的总数
        return count;
    }


    /**
     * 获取购物车中所有选中商品的总金额，然后减去优惠金额。
     *
     * @return BigDecimal 表示购物车中选中商品的总金额减去优惠后的金额。
     */
    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        // 计算选中商品的总价
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItemVo cartItem : items) {
                if (cartItem.getCheck()) {
                    amount = amount.add(cartItem.getTotalPrice());
                }
            }
        }
        // 返回总金额减去优惠金额
        return amount.subtract(getReduce());
    }

}
