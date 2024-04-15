package com.atguigu.common.constant;

import lombok.Data;

/**
 * ProductConstant类用于定义商品相关的常量。
 */
public class ProductConstant {

    /**
     * AttrEnum定义了商品的属性类型。
     * 包括基本属性和销售属性两种类型。
     */
    public enum AttrEnum {
        // 基本属性，编码为1
        ATTR_TYPE_BASE(1, "基本属性"),// 销售属性，编码为0
        ATTR_TYPE_SALE(0, "销售属性");

        // 属性类型的编码
        private int code;
        // 属性类型的描述信息
        private String msg;

        /**
         * 构造函数用于初始化属性类型。
         *
         * @param code 属性类型的编码。
         * @param msg  属性类型的描述信息。
         */
        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        /**
         * 获取属性类型的编码。
         *
         * @return 属性类型的编码。
         */
        public int getCode() {
            return code;
        }

        /**
         * 获取属性类型的描述信息。
         *
         * @return 属性类型的描述信息。
         */
        public String getMsg() {
            return msg;
        }
    }
}
