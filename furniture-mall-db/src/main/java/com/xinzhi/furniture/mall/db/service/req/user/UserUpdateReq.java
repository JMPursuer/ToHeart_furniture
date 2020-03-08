package com.xinzhi.furniture.mall.db.service.req.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserUpdateReq {
    /**
     * 用户ID
     */
    @NotNull
    @ApiModelProperty(value = "用户ID", required = true)
    private Integer userId;
    /**
     * 手机号
     */
    @NotBlank
    @ApiModelProperty(value = "手机号", required = true)
    private String mobile;

    @NotBlank
    @ApiModelProperty(value = "用户昵称", required = true)
    private String nickname;
    /**
     * 身份证
     */
    @NotBlank
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

    @ApiModelProperty(value = "折扣：0 < discount <= 1")
    private Float discount;

}
