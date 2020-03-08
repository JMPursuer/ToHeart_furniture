package com.xinzhi.furniture.mall.db.result;

public enum ErrorCodeEnum {

    SUCCESS("1000", "成功"),

    FAIL("1001", "系统异常"),

    USER_NOT_LOGIN("1002", "登录失效，请重新登录"),

    USER_NOT_EXIST("1003", "用户不存在"),

    INPUT_ERROR("1004", "请求参数错误"),

    LOGIN_ERROR("1005", "手机号或密码错误，登录失败"),

    PHONE_EXIST_ERROR("1006", "手机号已存在"),

    ID_CARD_EXIST_ERROR("1007", "身份证号已存在"),

    ADDRESS_NOT_EXIST_ERROR("1008", "地址不存在"),

    CART_EMPTY_ERROR("1009", "购物车为空"),

    ORDER_CAN_NOT_CANCEL_ERROR("1010", "订单不能取消"),

    ORDER_NOT_EXIST_ERROR("1011", "订单不存在"),

    ORDER_PAY_ERROR("1012", "订单支付失败"),

    TOP_UP_ERROR("1013", "充值失败"),

    ORDER_CAN_NOT_DELETE_ERROR("1014", "订单不能删除"),

    ORDER_COMFIRM_ERROR("1015", "订单确认失败"),

    USER_FORBIDDEN_LOGIN_ERROR("1016", "你已被禁止登录"),

    USER_ALREADY_RESET_PASSWORD_ERROR("1017", "你已设置过登录密码"),

    USER_PASSWORD_NOT_EQUAL_ERROR("1018", "登录密码输入不一致"),

    USER_STATUS_ERROR("1019", "用户状态异常"),

    PHONE_NOT_EXIST_ERROR("1020", "手机号不存在"),

    EXCEL_FORMAT_ERROR("1021", "Excel文件错误"),

    OPTION_TMP_NO_HANDLE_ERROR("1022", "临时规格数据还未处理完"),

    OPTION_INCOMPLETE_ERROR("1023", "规格数据不完整"),

    ;


    private String code;

    private String desc;

    ErrorCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return this.code;
    }


    public String getDesc() {
        return desc;
    }

}
