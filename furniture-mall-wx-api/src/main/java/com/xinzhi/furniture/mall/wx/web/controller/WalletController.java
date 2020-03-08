package com.xinzhi.furniture.mall.wx.web.controller;


import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.xinzhi.furniture.mall.core.util.IpUtil;
import com.xinzhi.furniture.mall.db.result.PageData;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.WalletService;
import com.xinzhi.furniture.mall.db.service.req.Page;
import com.xinzhi.furniture.mall.db.service.req.wallet.TopUpReq;
import com.xinzhi.furniture.mall.wx.annotation.LoginUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xinzhi.furniture.mall.db.service.resp.wallet.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/wx/wallet")
@Validated
@Api(tags = "钱包")
@Slf4j
public class WalletController {

    @Autowired
    private WalletService walletService;


    @ApiOperation(value = "钱包信息")
    @PostMapping("/info")
    public Result<WalletResp> getBalance(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId) {
        return walletService.getBalance(userId);
    }


    @ApiOperation(value = "钱包流水")
    @PostMapping("/flow")
    public Result<PageData<WalletFlowResp>> getWalletFlowList(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody Page req) {
        return walletService.getWalletFlowList(userId, req);
    }

    @ApiOperation(value = "钱包充值")
    @PostMapping("/topup")
    public Result<WxPayMpOrderResult> topUp(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody TopUpReq req, HttpServletRequest request) {
        return walletService.topUp(userId, req, IpUtil.getIpAddr(request));
    }

    @ApiOperation(value = "钱包充值回调地址")
    @PostMapping("/notify")
    public String payNotify(@RequestBody String bodyXml) {
        log.info(bodyXml);
        return walletService.payNotify(bodyXml);
    }

}
