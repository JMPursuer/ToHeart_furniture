package com.xinzhi.furniture.mall.admin.web.controller;

import com.xinzhi.furniture.mall.admin.annotation.RequiresPermissionsDesc;
import com.xinzhi.furniture.mall.db.domain.LitemallAdmin;
import com.xinzhi.furniture.mall.db.domain.LitemallWallet;
import com.xinzhi.furniture.mall.db.result.PageData;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.WalletService;
import com.xinzhi.furniture.mall.db.service.req.user.UserListReq;
import com.xinzhi.furniture.mall.db.service.req.wallet.AdminTopUpReq;
import com.xinzhi.furniture.mall.db.service.req.wallet.AdminWithdrawReq;
import com.xinzhi.furniture.mall.db.service.req.wallet.WalletFlowListReq;
import com.xinzhi.furniture.mall.db.service.req.wallet.WalletListReq;
import com.xinzhi.furniture.mall.db.service.resp.user.UserResp;
import com.xinzhi.furniture.mall.db.service.resp.wallet.WalletFlowResp;
import com.xinzhi.furniture.mall.db.service.resp.wallet.WalletListResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/wallet")
@Validated
@Api(tags = {"钱包管理"})
public class AdmWalletController {

    @Autowired
    private WalletService walletService;

    @ApiOperation(value = "钱包列表")
    @RequiresPermissions("admin:wallet:list")
    @RequiresPermissionsDesc(menu = {"用户管理", "财务管理"}, button = "钱包列表")
    @PostMapping("/list")
    public Result<PageData<WalletListResp>> getWalletList(@Valid @RequestBody WalletListReq req) {
        Result<PageData<WalletListResp>> result = walletService.getWalletList(req);
        return result;
    }

    @ApiOperation(value = "钱包流水列表")
    @RequiresPermissions("admin:wallet:flow:list")
    @RequiresPermissionsDesc(menu = {"用户管理", "财务管理"}, button = "钱包流水")
    @PostMapping("/flow/list")
    public Result<PageData<WalletFlowResp>> getWalletFlowList(@Valid @RequestBody WalletFlowListReq req) {
        Result<PageData<WalletFlowResp>> result = walletService.getWalletFlowList(req.getUserId(), req);
        return result;
    }

    @ApiOperation(value = "钱包充值")
    @RequiresPermissions("admin:wallet:topup")
    @RequiresPermissionsDesc(menu = {"用户管理", "财务管理"}, button = "充值")
    @PostMapping("/topup")
    public Result topup(@Valid @RequestBody AdminTopUpReq req) {
        return walletService.adminTopUp(req, ((LitemallAdmin)SecurityUtils.getSubject().getPrincipal()).getUsername());
    }

    @ApiOperation(value = "钱包提现")
    @RequiresPermissions("admin:wallet:withdraw")
    @RequiresPermissionsDesc(menu = {"用户管理", "财务管理"}, button = "提现")
    @PostMapping("/withdraw")
    public Result withdraw(@Valid @RequestBody AdminWithdrawReq req) {
        return walletService.adminWithdraw(req, ((LitemallAdmin)SecurityUtils.getSubject().getPrincipal()).getUsername());
    }

}
