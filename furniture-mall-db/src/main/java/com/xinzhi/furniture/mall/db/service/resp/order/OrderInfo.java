package com.xinzhi.furniture.mall.db.service.resp.order;

import com.xinzhi.furniture.mall.db.util.OrderHandleOption;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class OrderInfo {

    @ApiModelProperty(value = "订单ID")
    private Integer id;
    @ApiModelProperty(value = "订单编号")
    private String orderSn;
    @ApiModelProperty(value = "用户订单留言")
    private String message;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime addTime;
    @ApiModelProperty(value = "收货人名称")
    private String consignee;
    @ApiModelProperty(value = "收货人手机号")
    private String mobile;
    @ApiModelProperty(value = "收货具体地址")
    private String address;
    @ApiModelProperty(value = "商品总费用")
    private BigDecimal goodsPrice;
    @ApiModelProperty(value = "配送费用")
    private BigDecimal freightPrice;
    @ApiModelProperty(value = "实付费用")
    private BigDecimal actualPrice;
    @ApiModelProperty(value = "订单状态")
    private String orderStatusText;
    @ApiModelProperty(value = "订单可以操作的状态")
    private OrderHandleOption handleOption;
    
}
