package com.xinzhi.furniture.mall.db.service.req.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UserLoginReq {

    /**
     * 手机号
     */
    @NotEmpty
    @ApiModelProperty(value = "手机号", required = true)
    private String mobile;
    /**
     * 登录密码
     */
    @NotEmpty
    @ApiModelProperty(value = "登录密码", required = true)
    private String password;

}
