package com.xinzhi.furniture.mall.db.service.resp.wallet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletFlowResp {

    @ApiModelProperty(value = "流水记录：负数减少，正数增加")
    private BigDecimal flow;

    @ApiModelProperty(value = "当前余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "描述")
    private String desc;

    @ApiModelProperty(value = "操作者")
    private String optName;

    @ApiModelProperty(value = "备注")
    private String note;

}
