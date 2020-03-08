package com.xinzhi.furniture.mall.db.enumeration;

public enum TopUpStatusEnum {

    SUBMIT(0, "已提交"),

    PAID_SUCCESS(1, "支付成功"),

    PAID_FAIL(2, "支付失败");

    private int code;

    private String desc;

    TopUpStatusEnum(int code, String desc) {
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
