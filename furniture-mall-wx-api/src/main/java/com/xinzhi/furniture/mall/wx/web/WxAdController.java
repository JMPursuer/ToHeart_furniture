package com.xinzhi.furniture.mall.wx.web;

import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.LitemallAdService;
import com.xinzhi.furniture.mall.db.service.req.IdObject;
import com.xinzhi.furniture.mall.db.service.resp.ad.AdResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/wx/ad")
@Validated
@Api(tags = "广告")
public class WxAdController {

    @Autowired
    private LitemallAdService litemallAdService;

    @ApiOperation(value = "广告详情")
    @PostMapping("/detail")
    public Result<AdResp> detail(@RequestBody IdObject idObject) {
        return litemallAdService.findById("api", idObject.getId());
    }

}
