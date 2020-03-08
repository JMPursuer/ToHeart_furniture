package com.xinzhi.furniture.mall.db.service.req.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderReq {

    @ApiModelProperty(value = "订单号")
    @NotNull
    private Integer orderId;

}
