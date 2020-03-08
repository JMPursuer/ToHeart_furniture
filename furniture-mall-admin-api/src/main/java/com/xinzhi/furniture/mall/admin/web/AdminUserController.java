package com.xinzhi.furniture.mall.admin.web;

import com.xinzhi.furniture.mall.admin.annotation.RequiresPermissionsDesc;
import com.xinzhi.furniture.mall.core.util.ResponseUtil;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.req.user.UserAddReq;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.xinzhi.furniture.mall.core.validator.Order;
import com.xinzhi.furniture.mall.core.validator.Sort;
import com.xinzhi.furniture.mall.db.domain.LitemallUser;
import com.xinzhi.furniture.mall.db.service.LitemallUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

//@RestController
//@RequestMapping("/admin/user")
//@Validated
public class AdminUserController {
    private final Log logger = LogFactory.getLog(AdminUserController.class);

    @Autowired
    private LitemallUserService userService;

//    @RequiresPermissions("admin:user:list")
//    @RequiresPermissionsDesc(menu = {"用户管理", "会员管理"}, button = "查询")
//    @GetMapping("/list")
//    public Object list(String username, String mobile,
//                       @RequestParam(defaultValue = "1") Integer page,
//                       @RequestParam(defaultValue = "10") Integer limit,
//                       @Sort @RequestParam(defaultValue = "add_time") String sort,
//                       @Order @RequestParam(defaultValue = "desc") String order) {
//        List<LitemallUser> userList = userService.querySelective(username, mobile, page, limit, sort, order);
//        return ResponseUtil.okList(userList);
//    }


}
