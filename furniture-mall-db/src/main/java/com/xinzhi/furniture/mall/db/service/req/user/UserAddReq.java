package com.xinzhi.furniture.mall.db.service.req.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class UserAddReq {

    @ApiModelProperty(value = "用户性别：0-未知， 1-男， 2-女")
    private Byte gender;
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
