package com.xinzhi.furniture.mall.db.service.req.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class OrderPrepayReq extends OrderReq {

    @ApiModelProperty(value = "微信openid")
    @NotNull
    @NotEmpty
    private String openid;

}
