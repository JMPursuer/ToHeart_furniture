package com.xinzhi.furniture.mall.db.service.resp.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderGoods {

    private Integer id;
    @ApiModelProperty(value = "订单号")
    private Integer orderId;
    @ApiModelProperty(value = "商品ID")
    private Integer goodsId;
    @ApiModelProperty(value = "商品名称")
    private String goodsName;
    @ApiModelProperty(value = "商品编号")
    private String goodsSn;
    @ApiModelProperty(value = "货品ID")
    private Integer productId;
    @ApiModelProperty(value = "购买数量")
    private Short number;
    @ApiModelProperty(value = "售价")
    private BigDecimal price;
    @ApiModelProperty(value = "规格列表")
    private String[] specifications;
    @ApiModelProperty(value = "商品图片")
    private String picUrl;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime addTime;
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

}
