package com.atguigu.common.constant;

/**
 * 仓库常量类，定义了采购相关状态的枚举。
 */
public class WareConstant {

    /**
     * 定义采购状态的枚举，包括新建、已分配、已领取、已完成和有异常等状态。
     */
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"), // 采购单新建状态
        ASSIGNED(1,"已分配"), // 采购单已分配状态
        RECEIVE(2,"已领取"), // 采购单已领取状态
        FINISH(3,"已完成"), // 采购单已完成状态
        HASERROR(4,"有异常"); // 采购单有异常状态

        private int code; // 状态编码
        private String msg; // 状态信息

        /**
         * 构造函数，用于初始化采购状态。
         * @param code 状态编码
         * @param msg 状态信息
         */
        PurchaseStatusEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }

        /**
         * 获取状态编码。
         * @return 状态编码
         */
        public int getCode() {
            return code;
        }

        /**
         * 获取状态信息。
         * @return 状态信息
         */
        public String getMsg() {
            return msg;
        }
    }

    /**
     * 定义采购详情状态的枚举，包括新建、已分配、正在采购、已完成和采购失败等状态。
     */
    public enum PurchaseDetailStatusEnum{
        CREATED(0,"新建"), // 采购详情新建状态
        ASSIGNED(1,"已分配"), // 采购详情已分配状态
        BUYING(2,"正在采购"), // 采购详情正在采购状态
        FINISH(3,"已完成"), // 采购详情已完成状态
        HASERROR(4,"采购失败"); // 采购详情采购失败状态

        private int code; // 状态编码
        private String msg; // 状态信息

        /**
         * 构造函数，用于初始化采购详情状态。
         * @param code 状态编码
         * @param msg 状态信息
         */
        PurchaseDetailStatusEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }

        /**
         * 获取状态编码。
         * @return 状态编码
         */
        public int getCode() {
            return code;
        }

        /**
         * 获取状态信息。
         * @return 状态信息
         */
        public String getMsg() {
            return msg;
        }
    }
}
