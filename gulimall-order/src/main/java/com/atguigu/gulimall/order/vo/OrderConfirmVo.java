package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Data
public class OrderConfirmVo {

    @Getter @Setter
    /** 会员收获地址列表 **/
    List<MemberAddressVo> memberAddressVos;

    @Getter @Setter
    /** 所有选中的购物项 **/
    List<OrderItemVo> items;

    /** 发票记录 **/
    @Getter @Setter
    /** 优惠券（会员积分） **/
    private Integer integration;

    /** 防止重复提交的令牌 **/
    @Getter @Setter
    private String orderToken;

    @Getter @Setter
    Map<Long,Boolean> stocks;

    /**
     * 获取订单项的总数量。
     * <p>此方法不接受任何参数，它会计算当前订单中所有订单项的数量总和。</p>
     *
     * @return Integer 返回订单项的总数量。如果没有订单项，则返回0。
     */
    public Integer getCount() {
        Integer count = 0; // 初始化计数器为0
        // 检查订单项列表是否非空且不为空
        if (items != null && !items.isEmpty()) {
            // 遍历订单项列表，累加每个订单项的数量
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count; // 返回累计的订单项数量
    }


    /**
     * 计算订单中所有商品的总额。
     * <p>此方法不接受任何参数，它会遍历订单中的所有商品项（如果存在），
     * 对每项商品的价格乘以数量，然后累加得到订单的总额。</p>
     *
     * @return BigDecimal 表示订单的总额。
     */
    public BigDecimal getTotal() {
        BigDecimal totalNum = BigDecimal.ZERO; // 初始化订单总额为0
        if (items != null && !items.isEmpty()) { // 检查商品项列表是否非空
            for (OrderItemVo item : items) { // 遍历每个商品项
                // 计算当前商品项的总价格
                BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                // 累加所有商品项的总价格
                totalNum = totalNum.add(itemPrice);
            }
        }
        return totalNum; // 返回计算后的订单总额
    }


    /** 应付价格 **/
    //BigDecimal payPrice;
    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
