package com.xinzhi.furniture.mall.db.service.resp.wallet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletResp {

    @ApiModelProperty(value = "钱包余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "微信openid")
    private String openid;

}
