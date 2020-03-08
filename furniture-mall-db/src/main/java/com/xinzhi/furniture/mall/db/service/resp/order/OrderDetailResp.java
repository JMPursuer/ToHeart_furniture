package com.xinzhi.furniture.mall.db.service.resp.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailResp {

    @ApiModelProperty(value = "订单信息")
    private OrderInfo orderInfo;

    @ApiModelProperty(value = "订单商品")
    private List<OrderGoods> orderGoods;

}
