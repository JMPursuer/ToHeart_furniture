package com.xinzhi.furniture.mall.db.service.resp.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderSubmitResp {

    @ApiModelProperty(value = "订单号")
    private Integer orderId;

}
