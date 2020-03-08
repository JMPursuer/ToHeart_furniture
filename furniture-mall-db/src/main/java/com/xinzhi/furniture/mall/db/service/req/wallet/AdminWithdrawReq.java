package com.xinzhi.furniture.mall.db.service.req.wallet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class AdminWithdrawReq {

    @ApiModelProperty(value = "用户ID")
    @NotNull
    private Integer userId;

    @ApiModelProperty(value = "提现的金额")
    @NotNull
    private BigDecimal amount;

    @ApiModelProperty(value = "备注")
    private String note;

}
