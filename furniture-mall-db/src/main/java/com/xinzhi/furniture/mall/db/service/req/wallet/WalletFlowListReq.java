package com.xinzhi.furniture.mall.db.service.req.wallet;

import com.xinzhi.furniture.mall.db.service.req.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class WalletFlowListReq extends Page {

    @ApiModelProperty(value = "用户ID")
    @NotNull
    private Integer userId;

}
