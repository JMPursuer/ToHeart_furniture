package com.xinzhi.furniture.mall.db.service.resp.ad;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AdResp extends AdListResp {

    @ApiModelProperty(value = "内容")
    private String content;

}
