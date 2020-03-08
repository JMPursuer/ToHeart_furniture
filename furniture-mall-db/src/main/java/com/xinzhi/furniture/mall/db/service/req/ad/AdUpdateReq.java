package com.xinzhi.furniture.mall.db.service.req.ad;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AdUpdateReq extends AdAddReq {

    @ApiModelProperty(value = "ID值")
    @NotNull
    private Integer id;

}
