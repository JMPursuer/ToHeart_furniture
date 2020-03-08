package com.xinzhi.furniture.mall.db.service.resp.order;

import com.xinzhi.furniture.mall.db.util.OrderHandleOption;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderListResp {

    @ApiModelProperty(value = "订单ID")
    private Integer id;
    @ApiModelProperty(value = "订单编号")
    private String orderSn;
    @ApiModelProperty(value = "实付费用")
    private BigDecimal actualPrice;
    @ApiModelProperty(value = "订单状态")
    private String orderStatusText;
    @ApiModelProperty(value = "订单可以操作的状态")
    private OrderHandleOption handleOption;

    @ApiModelProperty(value = "商品列表")
    private List<OrderGoodsResp> goodsList;

}
