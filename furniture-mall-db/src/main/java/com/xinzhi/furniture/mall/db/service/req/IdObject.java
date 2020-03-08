package com.xinzhi.furniture.mall.db.service.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class IdObject {

    @ApiModelProperty(value = "ID值")
    @NotNull
    private Integer id;

}
