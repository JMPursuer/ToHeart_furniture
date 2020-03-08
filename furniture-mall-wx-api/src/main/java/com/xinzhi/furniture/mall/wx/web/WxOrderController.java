package com.xinzhi.furniture.mall.wx.web;

import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.xinzhi.furniture.mall.db.result.PageData;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.req.order.*;
import com.xinzhi.furniture.mall.db.service.resp.order.OrderDetailResp;
import com.xinzhi.furniture.mall.db.service.resp.order.OrderListResp;
import com.xinzhi.furniture.mall.db.service.resp.order.OrderSubmitResp;
import com.xinzhi.furniture.mall.wx.annotation.LoginUser;
import com.xinzhi.furniture.mall.wx.service.WxOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/wx/order")
@Validated
@Slf4j
@Api(tags = "订单")
public class WxOrderController {

    @Autowired
    private WxOrderService wxOrderService;

    /**
     * 订单列表
     *
     * @param userId   用户ID
     * @param req
     * @return 订单列表
     */
    @ApiOperation(value = "订单列表")
    @PostMapping("list")
    public Result<PageData<OrderListResp>> list(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody OrderListReq req) {
        return wxOrderService.list(userId, req.getShowType(), req.getPageIndex(), req.getPageSize());
    }

    /**
     * 订单详情
     *
     * @param userId  用户ID
     * @param req 订单
     * @return 订单详情
     */
    @ApiOperation(value = "订单详情")
    @PostMapping("detail")
    public Result<OrderDetailResp> detail(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody OrderReq req) {
        return wxOrderService.detail(userId, req);
    }

    /**
     * 提交订单
     *
     * @param userId 用户ID
     * @param req   订单信息
     * @return 提交订单操作结果
     */
    @ApiOperation(value = "提交订单")
    @PostMapping("submit")
    public Result<OrderSubmitResp> submit(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody OrderSubmitReq req) {
        return wxOrderService.submit(userId, req);
    }

    /**
     * 取消订单
     *
     * @param userId 用户ID
     * @param req   订单信息
     * @return 取消订单操作结果
     */
    @ApiOperation(value = "取消订单")
    @PostMapping("cancel")
    public Result<?> cancel(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody OrderCancelReq req) {
        return wxOrderService.cancel(userId, req);
    }

    /**
     * 付款订单的预支付会话标识
     *
     * @param userId 用户ID
     * @param req   订单信息，{ orderId：xxx }
     * @return 支付订单ID
     */
    @ApiOperation(value = "预支付")
    @PostMapping("prepay")
    public Result<WxPayMpOrderResult> prepay(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody OrderPrepayReq req, HttpServletRequest request) {
        return wxOrderService.prepay(userId, req, request);
    }

    /**
     * 微信H5支付
     * @param userId
     * @param body
     * @param request
     * @return
     */
//    @PostMapping("h5pay")
//    public Object h5pay(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody String body, HttpServletRequest request) {
//        return wxOrderService.h5pay(userId, body, request);
//    }

    /**
     * 微信付款成功或失败回调接口
     */
    @ApiOperation(value = "商品订单支付回调地址")
    @PostMapping("notify")
    public String payNotify(@RequestBody String bodyXml) {
        log.info(bodyXml);
        return wxOrderService.payNotify(bodyXml);
    }

    /**
     * 订单申请退款
     *
     * @param userId 用户ID
     * @param body   订单信息，{ orderId：xxx }
     * @return 订单退款操作结果
     */
//    @PostMapping("refund")
//    public Object refund(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody String body) {
//        return wxOrderService.refund(userId, body);
//    }

    /**
     * 确认收货
     *
     * @param userId 用户ID
     * @param req   订单信息，{ orderId：xxx }
     * @return 订单操作结果
     */
    @ApiOperation(value = "确认收货")
    @PostMapping("confirm")
    public Result confirm(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody OrderReq req) {
        return wxOrderService.confirm(userId, req);
    }

    /**
     * 删除订单
     *
     * @param userId 用户ID
     * @param req   订单信息，{ orderId：xxx }
     * @return 订单操作结果
     */
    @ApiOperation(value = "删除订单")
    @PostMapping("delete")
    public Result delete(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody OrderReq req) {
        return wxOrderService.delete(userId, req);
    }

    /**
     * 待评价订单商品信息
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @param goodsId 商品ID
     * @return 待评价订单商品信息
     */
//    @GetMapping("goods")
//    public Object goods(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId,
//                        @NotNull Integer orderId,
//                        @NotNull Integer goodsId) {
//        return wxOrderService.goods(userId, orderId, goodsId);
//    }

    /**
     * 评价订单商品
     *
     * @param userId 用户ID
     * @param body   订单信息，{ orderId：xxx }
     * @return 订单操作结果
     */
//    @PostMapping("comment")
//    public Object comment(@ApiParam(name = "userId", hidden = true) @LoginUser Integer userId, @RequestBody String body) {
//        return wxOrderService.comment(userId, body);
//    }

}