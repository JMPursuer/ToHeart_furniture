package com.xinzhi.furniture.mall.db.service.resp.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserLoginResp {
    /**
     * 登录凭证
     */
    @ApiModelProperty(value = "登录凭证")
    private String token;
    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码")
    private String mobile;
    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String nickname;
    /**
     * 用户类型：1-普通用户，2-公司用户
     */
    @ApiModelProperty(value = "用户类型：1-普通用户，2-公司用户")
    private Integer type;
    /**
     * 公司名称
     */
    @ApiModelProperty(value = "公司名称")
    private String company;
    /**
     * 公司地址
     */
    @ApiModelProperty(value = "公司地址")
    private String companyAddress;

    @ApiModelProperty(value = "用户性别：0 未知， 1男， 1 女")
    private Byte gender;

    @ApiModelProperty(value = "用户状态：0-正常，1-禁用，2-注销，3-未修改登录秘密")
    private Byte status;

    @ApiModelProperty(value = "折扣：0 < discount <= 1")
    private BigDecimal discount;

}
