package com.xinzhi.furniture.mall.db.service.resp.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderGoodsResp {

    private Integer id;
    @ApiModelProperty(value = "商品名称")
    private String goodsName;
    @ApiModelProperty(value = "购买数量")
    private Short number;
    @ApiModelProperty(value = "售价")
    private BigDecimal price;
    @ApiModelProperty(value = "规格列表")
    private String[] specifications;
    @ApiModelProperty(value = "商品图片")
    private String picUrl;

}
