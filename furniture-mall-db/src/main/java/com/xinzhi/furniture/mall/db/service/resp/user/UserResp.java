package com.xinzhi.furniture.mall.db.service.resp.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class UserResp {

    @ApiModelProperty(value = "用户ID", required = true)
    private Integer userId;
    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号", required = true)
    private String mobile;

    @ApiModelProperty(value = "昵称", required = true)
    private String nickname;

    /**
     * 身份证
     */
    @ApiModelProperty(value = "身份证", required = true)
    private String idCard;
    /**
     * 用户类型：1-普通用户，2-公司用户
     */
    @NotNull
    @ApiModelProperty(value = "用户类型：1-普通用户，2-公司用户", required = true)
    private Integer type;
    /**
     * 公司名称
     */
    @ApiModelProperty(value = "公司名称")
    private String company;
    /**
     * 公司地址
     */
    @ApiModelProperty(value = "公司名称")
    private String companyAddress;

    @ApiModelProperty(value = "用户状态：0-正常，1-禁用", required = true)
    private Byte status;

    @ApiModelProperty(value = "用户性别：0 未知， 1男， 1 女")
    private Byte gender;

    @ApiModelProperty(value = "折扣：0 < discount <= 1")
    private BigDecimal discount;

}
