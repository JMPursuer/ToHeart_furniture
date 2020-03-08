package com.xinzhi.furniture.mall.db.service.resp.wallet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletListResp {

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

}
