package com.xinzhi.furniture.mall.db.service.req.wallet;

import com.xinzhi.furniture.mall.db.service.req.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WalletListReq extends Page {

    @ApiModelProperty(value = "手机号")
    private String mobile;

}
