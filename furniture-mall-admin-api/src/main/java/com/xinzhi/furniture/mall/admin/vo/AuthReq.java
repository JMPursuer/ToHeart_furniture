package com.xinzhi.furniture.mall.admin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthReq {

    @ApiModelProperty(value = "用户名", example = "admin123")
    @NotBlank
    private String username;

    @ApiModelProperty(value = "密码", example = "admin123")
    @NotBlank
    private String password;

}
