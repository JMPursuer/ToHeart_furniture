package com.xinzhi.furniture.mall.db.enumeration;

public enum UserStatusEnum {

    OK(0, "正常"),

    FORBIDDEN(1, "禁用"),

    DELETE(2, "注销"),

    NOT_UPDATE_PASSOWRD(3, "未修改登录密码"),

    ;

    private int code;

    private String desc;

    UserStatusEnum(int code, String desc) {
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
