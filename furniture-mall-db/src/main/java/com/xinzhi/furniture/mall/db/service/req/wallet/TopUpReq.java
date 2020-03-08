package com.xinzhi.furniture.mall.db.service.req.wallet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TopUpReq {

    @ApiModelProperty(value = "微信openid")
    @NotEmpty
    @NotNull
    private String openid;

    @ApiModelProperty(value = "充值的金额")
    @NotNull
    private BigDecimal amount;

}
