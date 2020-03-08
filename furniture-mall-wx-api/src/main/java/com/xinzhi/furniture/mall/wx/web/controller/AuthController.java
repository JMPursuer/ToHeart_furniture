package com.xinzhi.furniture.mall.wx.web.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.xinzhi.furniture.mall.core.util.IpUtil;
import com.xinzhi.furniture.mall.db.domain.LitemallUser;
import com.xinzhi.furniture.mall.db.enumeration.UserStatusEnum;
import com.xinzhi.furniture.mall.db.result.ErrorCodeEnum;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.LitemallUserService;
import com.xinzhi.furniture.mall.db.service.req.user.UserLoginReq;
import com.xinzhi.furniture.mall.db.service.req.user.UserUpdatePasswordReq;
import com.xinzhi.furniture.mall.db.service.req.user.WechatAuthReq;
import com.xinzhi.furniture.mall.db.service.resp.user.UserLoginResp;
import com.xinzhi.furniture.mall.db.util.bcrypt.BCryptPasswordEncoder;
import com.xinzhi.furniture.mall.wx.annotation.LoginUser;
import com.xinzhi.furniture.mall.wx.service.UserTokenManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/wx/auth")
@Validated
@Slf4j
@Api(tags = "用户登录授权")
public class AuthController {

    @Autowired
    private LitemallUserService userService;

    @Autowired
    private WxMaService wxService;

    /**
     * 用户登录
     * @param userLoginReq
     * @param request
     * @return
     */
    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginResp> login(@Valid @RequestBody UserLoginReq userLoginReq, HttpServletRequest request) {

        List<LitemallUser> userList = userService.queryByMobile(userLoginReq.getMobile());
        LitemallUser user = null;
        if (userList.size() > 1) {
            return Result.fail();
        } else if (userList.size() == 0) {
            return Result.fail(ErrorCodeEnum.USER_NOT_EXIST);
        } else {
            user = userList.get(0);
        }

        if(user.getStatus() == UserStatusEnum.FORBIDDEN.getCode().byteValue()) {
            return Result.fail(ErrorCodeEnum.USER_FORBIDDEN_LOGIN_ERROR);
        }

        if(user.getStatus() == UserStatusEnum.DELETE.getCode().byteValue()) {
            return Result.fail(ErrorCodeEnum.USER_FORBIDDEN_LOGIN_ERROR);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(userLoginReq.getPassword(), user.getPassword())) {
            return Result.fail(ErrorCodeEnum.LOGIN_ERROR);
        }

        // 更新登录情况
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(IpUtil.getIpAddr(request));
        if (userService.updateById(user) == 0) {
            return Result.fail();
        }

        // token
        String token = UserTokenManager.generateToken(user.getId());
        UserLoginResp userLoginResp = new UserLoginResp();
        BeanUtils.copyProperties(user, userLoginResp);
        userLoginResp.setToken(token);

        return Result.ok().setResult(userLoginResp);
    }

    /**
     * 用户登出
     * @param userId
     * @return
     */
    @ApiOperation(value = "用户登出")
    @PostMapping("/logout")
    public Result<?> logout(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId) {
        // TODO: 2020/1/17 客户端删除掉token
        return Result.ok();
    }

    @ApiOperation(value = "修改密码")
    @PostMapping("/update/password")
    public Result updatePassword(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody UserUpdatePasswordReq req) {
        return userService.updatePassword(userId, req);
    }

    @ApiOperation(value = "微信授权")
    @PostMapping("/wechat")
    public Result wechat(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody WechatAuthReq req) {
        String code = req.getCode();
        if (StringUtils.isEmpty(code)) {
            return Result.fail(ErrorCodeEnum.INPUT_ERROR);
        }
        String sessionKey = null;
        String openId = null;
        try {
            WxMaJscode2SessionResult result = wxService.getUserService().getSessionInfo(code);
            sessionKey = result.getSessionKey();
            openId = result.getOpenid();
        } catch (Exception e) {
            log.error("wechat auth error", e);
            return Result.fail();
        }
        if (sessionKey == null || openId == null) {
            return Result.fail();
        }
        return userService.wechatAuth(userId, openId);
    }

}
