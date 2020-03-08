package com.xinzhi.furniture.mall.db.service.resp.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class GoodsOption {

    @ApiModelProperty(value = "面料选项列表")
    private List<GoodsOptionResp> p01List;

    @ApiModelProperty(value = "颜色选项列表")
    private List<GoodsOptionResp> p02List;

    @ApiModelProperty(value = "海绵选项列表")
    private List<GoodsOptionResp> p03List;

}
