package com.xinzhi.furniture.mall.db.enumeration;

public enum FlowTypeEnum {

    WECHAT_TOP_UP(1, "微信充值"),

    ADMIN_TOP_UP(2, "管理员操作充值"),

    PAID_GOODS(3, "购买商品支付"),

    ADMIN_WITHDRAW(4, "管理员操作提现")

    ;

    private int code;

    private String desc;

    FlowTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getDesc() {
        return desc;
    }

}
