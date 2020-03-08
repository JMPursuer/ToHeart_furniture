package com.xinzhi.furniture.mall.db.result;

import lombok.Data;

import java.io.Serializable;

@Data
public abstract class BaseResult<T> implements Serializable {
    /**
     * false：失败
     * true：成功
     */
    private boolean success = false;

    private String msg;

    private String code;

    private T data;
    /**
     * 服务器时间
     */
    private long ts;

}
