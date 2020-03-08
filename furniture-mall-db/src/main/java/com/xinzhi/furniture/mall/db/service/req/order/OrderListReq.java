package com.xinzhi.furniture.mall.db.service.req.order;

import com.xinzhi.furniture.mall.db.service.req.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderListReq extends Page {

    @ApiModelProperty(value = "0-全部订单, 1-待付款, 2-待发货, 3-待收货, 4-待评价")
    private Integer showType = 0;

}
