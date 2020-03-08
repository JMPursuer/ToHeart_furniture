package com.xinzhi.furniture.mall.db.service.req.user;

import com.xinzhi.furniture.mall.db.service.req.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserListReq extends Page {

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "身份证")
    private String idCard;

}
