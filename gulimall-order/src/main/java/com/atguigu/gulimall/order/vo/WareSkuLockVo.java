package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;



/**
 * 用于表示库存锁定信息的类。
 *
 * @author
 * @since
 */
@Data
public class WareSkuLockVo {

    private String orderSn; // 订单编号

    /**
     * 需要锁住的所有库存信息。
     * 每个库存信息包含商品的详细信息和需要锁定的数量。
     **/
    private List<OrderItemVo> locks;

}
