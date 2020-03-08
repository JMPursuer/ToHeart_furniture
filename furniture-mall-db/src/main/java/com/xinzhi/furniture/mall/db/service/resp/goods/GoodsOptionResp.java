package com.xinzhi.furniture.mall.db.service.resp.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GoodsOptionResp {

    @ApiModelProperty(value = "类型：PP是款式层，P01是面料层，P02是颜色层，P03是海绵层")
    private Integer type;

    @ApiModelProperty(value = "面料：M是布料，F是皮料，N是其他类")
    private String material;

    @ApiModelProperty(value = "选项CODE值")
    private String code;

    @ApiModelProperty(value = "图片")
    private String image;

    @ApiModelProperty(value = "描述")
    private String desc;

    @ApiModelProperty(value = "状态：0-无效，1-有效")
    private Integer status;

}
