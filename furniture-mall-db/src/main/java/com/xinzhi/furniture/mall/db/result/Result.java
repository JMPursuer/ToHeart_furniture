package com.xinzhi.furniture.mall.db.result;


import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public class Result<T> extends BaseResult implements Serializable {

    public Result() {

    }

    public Result(boolean succuss, String msg) {
        this.setSuccess(succuss);
        this.setMsg(msg);
    }

    public Result(boolean succuss) {
        this.setSuccess(succuss);
    }

    public Result(String code, String msg) {
        this.setCode(code);
        this.setMsg(msg);
    }

    public Result(boolean success, String msg, T data) {
        this.setSuccess(success);
        this.setMsg(msg);
        this.setData(data);
    }

    public static Result ok() {
        return ok(ErrorCodeEnum.SUCCESS.getCode(), ErrorCodeEnum.SUCCESS.getDesc());
    }

    public static <T> Result<T> ok(String code, String msg) {
        return baseCreate(code, msg, true);
    }

    public static Result fail() {
        return fail(ErrorCodeEnum.FAIL.getCode(), ErrorCodeEnum.FAIL.getDesc());
    }

    public static Result fail(String code, String msg) {
        return baseCreate(code, msg, false);
    }

    public static Result fail(ErrorCodeEnum errorCodeEnum) {
        return baseCreate(errorCodeEnum.getCode(), errorCodeEnum.getDesc(), false);
    }

    private static <T> Result<T> baseCreate(String code, String msg, boolean success) {
        Result result = new Result();
        result.setCode(code);
        result.setSuccess(success);
        result.setMsg(msg);
        result.setTs(Instant.now().toEpochMilli());
        return result;
    }

    public Result<T> setResult(T data) {
        this.setData(data);
        return this;
    }

    public T getData() {
        return (T) super.getData();
    }

}
