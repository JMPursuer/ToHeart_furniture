package com.xinzhi.furniture.mall.db.service.req.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderSubmitReq {

    @ApiModelProperty(value = "购物车ID")
    @NotNull
    private Integer cartId;

    @ApiModelProperty(value = "地址ID")
    @NotNull
    private Integer addressId;

    @ApiModelProperty(value = "备注留言")
    private String message;

}
