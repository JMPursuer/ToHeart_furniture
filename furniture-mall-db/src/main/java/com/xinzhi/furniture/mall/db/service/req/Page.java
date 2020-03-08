package com.xinzhi.furniture.mall.db.service.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class Page {

    @ApiModelProperty(value = "页码", required = true, example = "1")
    @NotNull
    @Min(1)
    private Integer pageIndex;

    @ApiModelProperty(value = "每页大小", required = true, example = "10")
    @NotNull
    @Max(100)
    private Integer pageSize;

}
