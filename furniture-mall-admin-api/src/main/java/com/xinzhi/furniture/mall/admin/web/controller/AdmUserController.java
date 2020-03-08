package com.xinzhi.furniture.mall.admin.web.controller;

import com.xinzhi.furniture.mall.admin.annotation.RequiresPermissionsDesc;
import com.xinzhi.furniture.mall.db.enumeration.UserStatusEnum;
import com.xinzhi.furniture.mall.db.result.ErrorCodeEnum;
import com.xinzhi.furniture.mall.db.result.PageData;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.req.user.UserAddReq;
import com.xinzhi.furniture.mall.db.service.req.user.UserListReq;
import com.xinzhi.furniture.mall.db.service.req.user.UserStatusUpdateReq;
import com.xinzhi.furniture.mall.db.service.req.user.UserUpdateReq;
import com.xinzhi.furniture.mall.db.service.resp.user.UserResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.xinzhi.furniture.mall.db.service.LitemallUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/user")
@Validated
@Api(tags = {"用户管理"})
public class AdmUserController {
    private final Log logger = LogFactory.getLog(AdmUserController.class);

    @Autowired
    private LitemallUserService userService;

    /**
     * 添加用户
     * @param userAddReq
     * @return
     */
    @ApiOperation(value = "添加用户")
    @RequiresPermissions("admin:user:create")
    @RequiresPermissionsDesc(menu = {"用户管理", "用户管理"}, button = "添加")
    @PostMapping("/add")
    public Result<?> addUser(@Valid @RequestBody UserAddReq userAddReq) {
        Result result = userService.addUser(userAddReq);
        return result;
    }

    /**
     * 更新用户信息
     * @param userUpdateReq
     * @return
     */
    @ApiOperation(value = "更新用户信息")
    @RequiresPermissions("admin:user:update")
    @RequiresPermissionsDesc(menu = {"用户管理", "用户管理"}, button = "修改")
    @PostMapping("/update")
    public Result<?> updateUser(@Valid @RequestBody UserUpdateReq userUpdateReq) {
        Result result = userService.updateUser(userUpdateReq);
        return result;
    }

    /**
     * 修改用户状态
     * @param userStatusUpdateReq
     * @return
     */
    @ApiOperation(value = "修改用户状态")
    @RequiresPermissions("admin:user:update:status")
    @RequiresPermissionsDesc(menu = {"用户管理", "用户管理"}, button = "禁用")
    @PostMapping("/update/status")
    public Result<?> updateUserStatus(@Valid @RequestBody UserStatusUpdateReq userStatusUpdateReq) {
        boolean check = false;
        for(UserStatusEnum userStatusEnum : UserStatusEnum.values()) {
            if(userStatusUpdateReq.getStatus() == userStatusEnum.getCode()) {
                check = true;
            }
        }
        if(!check) {
            return Result.fail(ErrorCodeEnum.INPUT_ERROR);
        }
        Result result = userService.updateUserStatus(userStatusUpdateReq);
        return result;
    }


    @ApiOperation(value = "用户列表")
    @RequiresPermissions("admin:user:list")
    @RequiresPermissionsDesc(menu = {"用户管理", "用户管理"}, button = "列表")
    @PostMapping("/list")
    public Result<PageData<UserResp>> getUserList(@Valid @RequestBody UserListReq req) {
        Result<PageData<UserResp>> result = userService.getUserList(req);
        return result;
    }

}
