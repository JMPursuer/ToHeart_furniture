package com.xinzhi.furniture.mall.db.service.req.ad;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AdAddReq {

    @ApiModelProperty(value = "广告标题")
    @NotNull
    @NotBlank
    private String name;
    @ApiModelProperty(value = "内容")
    @NotNull
    @NotBlank
    private String content;
    @ApiModelProperty(value = "所广告的商品页面或者活动页面链接地址")
    private String link;
    @ApiModelProperty(value = "广告宣传图片")
    private String url;
    @ApiModelProperty(value = "广告位置：1则是首页")
    private Byte position;
    @ApiModelProperty(value = "广告开始时间")
    private LocalDateTime startTime;
    @ApiModelProperty(value = "广告结束时间")
    private LocalDateTime endTime;
    @ApiModelProperty(value = "是否启动")
    private Boolean enabled;

}
