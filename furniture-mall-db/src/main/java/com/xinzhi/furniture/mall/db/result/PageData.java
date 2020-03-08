package com.xinzhi.furniture.mall.db.result;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class PageData<T> {

    @ApiModelProperty(value = "页码")
    private int pageIndex;

    @ApiModelProperty(value = "每页大小")
    private int pageSize;

    @ApiModelProperty(value = "总数量")
    private long total;

    @ApiModelProperty(value = "数据列表")
    private List<T> list;

}
