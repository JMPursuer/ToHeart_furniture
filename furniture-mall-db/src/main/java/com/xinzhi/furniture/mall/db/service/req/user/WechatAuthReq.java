package com.xinzhi.furniture.mall.db.service.req.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class WechatAuthReq {

    @NotBlank
    @NotNull
    private String code;

}
