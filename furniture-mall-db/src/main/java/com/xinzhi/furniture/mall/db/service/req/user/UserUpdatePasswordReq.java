package com.xinzhi.furniture.mall.db.service.req.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserUpdatePasswordReq {

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "确认密码")
    private String repassword;

}
