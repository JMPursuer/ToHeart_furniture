package com.xinzhi.furniture.mall.db.service.req.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserStatusUpdateReq {

    /**
     * 用户ID
     */
    @NotNull
    @ApiModelProperty(value = "用户ID", required = true)
    private Integer userId;
    /**
     * 用户状态：0-正常，1-禁用
     */
    @NotNull
    @ApiModelProperty(value = "用户状态：0-正常，1-禁用，2-注销，3-未修改登录秘密", required = true)
    private Integer status;

}
