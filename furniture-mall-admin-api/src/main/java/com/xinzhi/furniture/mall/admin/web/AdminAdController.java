package com.xinzhi.furniture.mall.admin.web;

import com.xinzhi.furniture.mall.admin.annotation.RequiresPermissionsDesc;
import com.xinzhi.furniture.mall.core.util.ResponseUtil;
import com.xinzhi.furniture.mall.db.result.PageData;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.req.IdObject;
import com.xinzhi.furniture.mall.db.service.req.ad.AdAddReq;
import com.xinzhi.furniture.mall.db.service.req.ad.AdListReq;
import com.xinzhi.furniture.mall.db.service.req.ad.AdUpdateReq;
import com.xinzhi.furniture.mall.db.service.resp.ad.AdListResp;
import com.xinzhi.furniture.mall.db.service.resp.ad.AdResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.xinzhi.furniture.mall.db.service.LitemallAdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ad")
@Validated
@Api(tags = "广告管理")
public class AdminAdController {
    private final Log logger = LogFactory.getLog(AdminAdController.class);

    @Autowired
    private LitemallAdService adService;

    @RequiresPermissions("admin:ad:list")
    @RequiresPermissionsDesc(menu = {"推广管理", "广告管理"}, button = "查询")
    @PostMapping("/list")
    @ApiOperation(value = "广告列表")
    public Result<PageData<AdListResp>> list(@RequestBody AdListReq req) {
        return adService.querySelective(req);
    }

    @RequiresPermissions("admin:ad:create")
    @RequiresPermissionsDesc(menu = {"推广管理", "广告管理"}, button = "添加")
    @PostMapping("/create")
    @ApiOperation(value = "创建广告")
    public Result<AdResp> create(@RequestBody AdAddReq ad) {
        return adService.add(ad);
    }

    @RequiresPermissions("admin:ad:read")
    @RequiresPermissionsDesc(menu = {"推广管理", "广告管理"}, button = "详情")
    @PostMapping("/read")
    @ApiOperation(value = "广告详情")
    public Result<AdResp> read(@RequestBody IdObject idObject) {
        return adService.findById("admin", idObject.getId());
    }

    @RequiresPermissions("admin:ad:update")
    @RequiresPermissionsDesc(menu = {"推广管理", "广告管理"}, button = "编辑")
    @PostMapping("/update")
    @ApiOperation(value = "广告修改")
    public Result<AdResp> update(@RequestBody AdUpdateReq ad) {
        return adService.updateById(ad);
    }

    @RequiresPermissions("admin:ad:delete")
    @RequiresPermissionsDesc(menu = {"推广管理", "广告管理"}, button = "删除")
    @PostMapping("/delete")
    @ApiOperation(value = "广告删除")
    public Object delete(@RequestBody IdObject idObject) {
        adService.deleteById(idObject.getId());
        return ResponseUtil.ok();
    }

}
