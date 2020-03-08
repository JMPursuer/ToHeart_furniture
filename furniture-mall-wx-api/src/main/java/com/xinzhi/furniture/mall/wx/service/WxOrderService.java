package com.xinzhi.furniture.mall.wx.service;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.order.WxPayMwebOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.google.common.collect.Lists;
import com.xinzhi.furniture.mall.core.notify.NotifyService;
import com.xinzhi.furniture.mall.core.notify.NotifyType;
import com.xinzhi.furniture.mall.core.qcode.QCodeService;
import com.xinzhi.furniture.mall.core.system.SystemConfig;
import com.xinzhi.furniture.mall.core.task.TaskService;
import com.xinzhi.furniture.mall.core.util.DateTimeUtil;
import com.xinzhi.furniture.mall.core.util.IpUtil;
import com.xinzhi.furniture.mall.core.util.JacksonUtil;
import com.xinzhi.furniture.mall.core.util.ResponseUtil;
import com.xinzhi.furniture.mall.db.dao.LitemallWalletMapper;
import com.xinzhi.furniture.mall.db.domain.*;
import com.xinzhi.furniture.mall.db.result.ErrorCodeEnum;
import com.xinzhi.furniture.mall.db.result.PageData;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.*;
import com.xinzhi.furniture.mall.db.service.req.order.OrderCancelReq;
import com.xinzhi.furniture.mall.db.service.req.order.OrderPrepayReq;
import com.xinzhi.furniture.mall.db.service.req.order.OrderReq;
import com.xinzhi.furniture.mall.db.service.req.order.OrderSubmitReq;
import com.xinzhi.furniture.mall.db.service.resp.order.*;
import com.xinzhi.furniture.mall.db.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.xinzhi.furniture.mall.core.express.ExpressService;
import com.xinzhi.furniture.mall.core.express.dao.ExpressInfo;
import com.xinzhi.furniture.mall.wx.task.OrderUnpaidTask;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xinzhi.furniture.mall.wx.util.WxResponseCode.*;

/**
 * 订单服务
 *
 * <p>
 * 订单状态：
 * 101 订单生成，未支付；102，下单后未支付用户取消；103，下单后未支付超时系统自动取消
 * 201 支付完成，商家未发货；202，订单生产，已付款未发货，但是退款取消；
 * 301 商家发货，用户未确认；
 * 401 用户确认收货； 402 用户没有确认收货超过一定时间，系统自动确认收货；
 *
 * <p>
 * 用户操作：
 * 当101用户未付款时，此时用户可以进行的操作是取消订单，或者付款操作
 * 当201支付完成而商家未发货时，此时用户可以取消订单并申请退款
 * 当301商家已发货时，此时用户可以有确认收货的操作
 * 当401用户确认收货以后，此时用户可以进行的操作是删除订单，评价商品，或者再次购买
 * 当402系统自动确认收货以后，此时用户可以删除订单，评价商品，或者再次购买
 *
 * <p>
 * 注意：目前不支持订单退货和售后服务
 */
@Service
@Transactional
@Slf4j
public class WxOrderService {
    private final Log logger = LogFactory.getLog(WxOrderService.class);

    @Autowired
    private LitemallUserService userService;
    @Autowired
    private LitemallOrderService orderService;
    @Autowired
    private LitemallOrderGoodsService orderGoodsService;
    @Autowired
    private LitemallAddressService addressService;
    @Autowired
    private LitemallCartService cartService;
    @Autowired
    private LitemallGoodsProductService productService;
    @Autowired
    private WxPayService wxPayService;
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private LitemallGrouponRulesService grouponRulesService;
    @Autowired
    private LitemallGrouponService grouponService;
    @Autowired
    private QCodeService qCodeService;
    @Autowired
    private ExpressService expressService;
    @Autowired
    private LitemallCommentService commentService;
    @Autowired
    private TaskService taskService;

    @Autowired
    private LitemallWalletMapper litemallWalletMapper;

    /**
     * 订单列表
     *
     * @param userId   用户ID
     * @param showType 订单信息：
     *                 0，全部订单；
     *                 1，待付款；
     *                 2，待发货；
     *                 3，待收货；
     *                 4，待评价。
     * @param pageIndex     分页页数
     * @param pageSize     分页大小
     * @return 订单列表
     */
    public Result<PageData<OrderListResp>> list(Integer userId, Integer showType, Integer pageIndex, Integer pageSize) {
        List<Short> orderStatus = OrderUtil.orderStatus(showType);
        List<LitemallOrder> orderList = orderService.queryByOrderStatus(userId, orderStatus, pageIndex, pageSize);
        long count = orderService.queryCountByOrderStatus(userId, orderStatus);

        List<OrderListResp> orderListRespList = Lists.newArrayList();
        for (LitemallOrder o : orderList) {

            OrderListResp orderListResp = new OrderListResp();
            orderListResp.setId(o.getId());
            orderListResp.setActualPrice(o.getActualPrice());
            orderListResp.setHandleOption(OrderUtil.build(o));
            orderListResp.setOrderStatusText(OrderUtil.orderStatusText(o));
            orderListResp.setOrderSn(o.getOrderSn());

            List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(o.getId());
            List<OrderGoodsResp> orderGoodsRespList = Lists.newArrayList();
            for (LitemallOrderGoods orderGoods : orderGoodsList) {
                OrderGoodsResp orderGoodsResp = new OrderGoodsResp();
                orderGoodsResp.setId(orderGoods.getId());
                orderGoodsResp.setGoodsName(orderGoods.getGoodsName());
                orderGoodsResp.setNumber(orderGoods.getNumber());
                orderGoodsResp.setPicUrl(orderGoods.getPicUrl());
                orderGoodsResp.setSpecifications(orderGoods.getSpecifications());
                orderGoodsResp.setPrice(orderGoods.getPrice());

                orderGoodsRespList.add(orderGoodsResp);
            }
            orderListResp.setGoodsList(orderGoodsRespList);

            orderListRespList.add(orderListResp);
        }

        PageData<OrderListResp> pageData = new PageData<>();
        pageData.setList(orderListRespList);
        pageData.setTotal(count);
        pageData.setPageSize(pageIndex);
        pageData.setPageIndex(pageIndex);

        return Result.ok().setResult(pageData);
    }

    /**
     * 订单详情
     *
     * @param userId  用户ID
     * @param req 订单ID
     * @return 订单详情
     */
    public Result<OrderDetailResp> detail(Integer userId, OrderReq req) {
        // 订单信息
        LitemallOrder order = orderService.findById(req.getOrderId());
        if (null == order) {
            return Result.fail(ErrorCodeEnum.ORDER_NOT_EXIST_ERROR);
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail(ErrorCodeEnum.ORDER_NOT_EXIST_ERROR);
        }
        OrderDetailResp orderDetailResp = new OrderDetailResp();

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setActualPrice(order.getActualPrice());
        orderInfo.setAddress(order.getAddress());
        orderInfo.setAddTime(order.getAddTime());
        orderInfo.setConsignee(order.getConsignee());
        orderInfo.setFreightPrice(order.getFreightPrice());
        orderInfo.setGoodsPrice(order.getGoodsPrice());
        orderInfo.setHandleOption(OrderUtil.build(order));
        orderInfo.setId(order.getId());
        orderInfo.setMessage(order.getMessage());
        orderInfo.setMobile(order.getMobile());
        orderInfo.setOrderSn(order.getOrderSn());
        orderInfo.setOrderStatusText(OrderUtil.orderStatusText(order));

        orderDetailResp.setOrderInfo(orderInfo);

        List<OrderGoods> ogList = Lists.newArrayList();
        List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(order.getId());
        for(LitemallOrderGoods orderGoods : orderGoodsList) {
            OrderGoods goods = new OrderGoods();
            BeanUtils.copyProperties(orderGoods, goods);
            ogList.add(goods);
        }
        orderDetailResp.setOrderGoods(ogList);

        return Result.ok().setResult(orderDetailResp);

    }

    /**
     * 提交订单
     * <p>
     * 1. 创建订单表项和订单商品表项;
     * 2. 购物车清空;
     * 3. 商品货品库存减少;
     *
     * @param userId 用户ID
     * @param req
     * @return 提交订单操作结果
     */
    @Transactional
    public Result<OrderSubmitResp> submit(Integer userId, OrderSubmitReq req) {

        Integer cartId = req.getCartId();
        Integer addressId = req.getAddressId();
        String message = req.getMessage();

        LitemallUser user = userService.findById(userId);
        if(user == null) {
            return Result.fail(ErrorCodeEnum.USER_NOT_EXIST);
        }

        // 收货地址
        LitemallAddress checkedAddress = addressService.query(userId, addressId);
        if (checkedAddress == null) {
            return Result.fail(ErrorCodeEnum.ADDRESS_NOT_EXIST_ERROR);
        }

        // 货品价格
        List<LitemallCart> checkedGoodsList = null;
        if (cartId.equals(0)) {
            checkedGoodsList = cartService.queryByUidAndChecked(userId);
        } else {
            LitemallCart cart = cartService.findById(cartId);
            checkedGoodsList = new ArrayList<>(1);
            checkedGoodsList.add(cart);
        }
        if (checkedGoodsList.size() == 0) {
            return Result.fail(ErrorCodeEnum.CART_EMPTY_ERROR);
        }
        BigDecimal checkedGoodsPrice = new BigDecimal(0);
        for (LitemallCart checkGoods : checkedGoodsList) {
            checkedGoodsPrice = checkedGoodsPrice.add(checkGoods.getPrice().multiply(new BigDecimal(checkGoods.getNumber())));
        }
        // 用户的折扣，不用的用户对应的折扣不一样
        checkedGoodsPrice = checkedGoodsPrice.multiply(user.getDiscount());

        // 根据订单商品总价计算运费，满足条件（例如88元）则免运费，否则需要支付运费（例如8元）；
        BigDecimal freightPrice = new BigDecimal(0);
        if (checkedGoodsPrice.compareTo(SystemConfig.getFreightLimit()) < 0) {
            freightPrice = SystemConfig.getFreight();
        }

        // 订单费用
        BigDecimal orderTotalPrice = checkedGoodsPrice.add(freightPrice).max(new BigDecimal(0));
        // 最终支付费用
        BigDecimal actualPrice = orderTotalPrice;

        Integer orderId = null;
        LitemallOrder order = null;
        // 订单
        order = new LitemallOrder();
        order.setUserId(userId);
        order.setOrderSn(orderService.generateOrderSn(userId));
        order.setOrderStatus(OrderUtil.STATUS_CREATE);
        order.setConsignee(checkedAddress.getName());
        order.setMobile(checkedAddress.getTel());
        order.setMessage(message);
        String detailedAddress = checkedAddress.getProvince() + checkedAddress.getCity() + checkedAddress.getCounty() + " " + checkedAddress.getAddressDetail();
        order.setAddress(detailedAddress);
        order.setGoodsPrice(checkedGoodsPrice);
        order.setFreightPrice(freightPrice);
        order.setOrderPrice(orderTotalPrice);
        order.setActualPrice(actualPrice);

        // 添加订单表项
        orderService.add(order);
        orderId = order.getId();

        // 添加订单商品表项
        for (LitemallCart cartGoods : checkedGoodsList) {
            // 订单商品
            LitemallOrderGoods orderGoods = new LitemallOrderGoods();
            orderGoods.setOrderId(order.getId());
            orderGoods.setGoodsId(cartGoods.getGoodsId());
            orderGoods.setGoodsSn(cartGoods.getGoodsSn());
            orderGoods.setProductId(cartGoods.getProductId());
            orderGoods.setGoodsName(cartGoods.getGoodsName());
            orderGoods.setPicUrl(cartGoods.getPicUrl());
            orderGoods.setPrice(cartGoods.getPrice());
            orderGoods.setNumber(cartGoods.getNumber());
            orderGoods.setSpecifications(cartGoods.getSpecifications());
            orderGoods.setAddTime(LocalDateTime.now());

            orderGoodsService.add(orderGoods);
        }

        // 删除购物车里面的商品信息
        cartService.clearGoods(userId);

        // 商品货品数量减少
        for (LitemallCart checkGoods : checkedGoodsList) {
            Integer productId = checkGoods.getProductId();
            LitemallGoodsProduct product = productService.findById(productId);

            // TODO: 2020/1/30 商品库存待处理

//            int remainNumber = product.getNumber() - checkGoods.getNumber();
//            if (remainNumber < 0) {
//                throw new RuntimeException("下单的商品货品数量大于库存量");
//            }
            if (productService.reduceStock(productId, checkGoods.getNumber()) == 0) {
                throw new RuntimeException("商品货品库存减少失败");
            }
        }

        // 订单支付超期任务
        taskService.addTask(new OrderUnpaidTask(orderId));

        OrderSubmitResp orderSubmitResp = new OrderSubmitResp();
        orderSubmitResp.setOrderId(orderId);
        return Result.ok().setResult(orderSubmitResp);
    }

    /**
     * 取消订单
     * <p>
     * 1. 检测当前订单是否能够取消；
     * 2. 设置订单取消状态；
     * 3. 商品货品库存恢复；
     * 4. TODO 优惠券；
     *
     * @param userId 用户ID
     * @param req   订单信息，{ orderId：xxx }
     * @return 取消订单操作结果
     */
    @Transactional
    public Result<?> cancel(Integer userId, OrderCancelReq req) {
        Integer orderId = req.getOrderId();
        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return Result.fail(ErrorCodeEnum.INPUT_ERROR);
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail(ErrorCodeEnum.INPUT_ERROR);
        }

        LocalDateTime preUpdateTime = order.getUpdateTime();

        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isCancel()) {
            return Result.fail(ErrorCodeEnum.ORDER_CAN_NOT_CANCEL_ERROR);
        }

        // 设置订单已取消状态
        order.setOrderStatus(OrderUtil.STATUS_CANCEL);
        order.setEndTime(LocalDateTime.now());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            throw new RuntimeException("更新数据已失效");
        }

        // 商品货品数量增加
        List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(orderId);
        for (LitemallOrderGoods orderGoods : orderGoodsList) {
            // TODO: 2020/1/30 商品库存待处理
            Integer productId = orderGoods.getProductId();
            Short number = orderGoods.getNumber();
            if (productService.addStock(productId, number) == 0) {
                throw new RuntimeException("商品货品库存增加失败");
            }
        }

        return Result.ok();
    }

    /**
     * 付款订单的预支付会话标识
     * <p>
     * 1. 检测当前订单是否能够付款
     * 2. 微信商户平台返回支付订单ID
     * 3. 设置订单付款状态
     *
     * @param userId 用户ID
     * @param req   订单信息，{ orderId：xxx }
     * @return 支付订单ID
     */
    @Transactional
    public Result<WxPayMpOrderResult> prepay(Integer userId, OrderPrepayReq req, HttpServletRequest request) {
        Integer orderId = req.getOrderId();
        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return Result.fail(ErrorCodeEnum.ORDER_NOT_EXIST_ERROR);
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail(ErrorCodeEnum.ORDER_NOT_EXIST_ERROR);
        }

        // 检测是否能够支付
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isPay()) {
            return Result.fail(ErrorCodeEnum.ORDER_PAY_ERROR);
        }

        // TODO: 2020/1/30 优先得考虑钱包支付，钱包支付完后，不够在微信支付
        LitemallWallet wallet = litemallWalletMapper.selectOneByExampleSelective(LitemallWalletExample.newAndCreateCriteria().andUserIdEqualTo(userId).example());
        if(wallet != null) {

        }


        WxPayMpOrderResult result = null;
        try {
            WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
            orderRequest.setOutTradeNo(order.getOrderSn());
            orderRequest.setOpenid(req.getOpenid());
            orderRequest.setNotifyUrl(wxPayService.getConfig().getNotifyUrl() + "/wx/order/notify");
            orderRequest.setBody("订单：" + order.getOrderSn());
            // 元转成分
            int fee = 0;
            BigDecimal actualPrice = order.getActualPrice();
            fee = actualPrice.multiply(new BigDecimal(100)).intValue();
            orderRequest.setTotalFee(fee);
            orderRequest.setSpbillCreateIp(IpUtil.getIpAddr(request));

            result = wxPayService.createOrder(orderRequest);
        } catch (Exception e) {
            log.error("订单支付失败", e);
            return Result.fail(ErrorCodeEnum.ORDER_PAY_ERROR);
        }

        if (orderService.updateWithOptimisticLocker(order) == 0) {
            log.error("订单更新失败：" + order.getId());
            return Result.fail(ErrorCodeEnum.ORDER_PAY_ERROR);
        }
        return Result.ok().setResult(result);
    }

    /**
     * 微信H5支付
     *
     * @param userId
     * @param body
     * @param request
     * @return
     */
    @Transactional
    public Object h5pay(Integer userId, String body, HttpServletRequest request) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgumentValue();
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.badArgumentValue();
        }

        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isPay()) {
            return ResponseUtil.fail(ORDER_INVALID_OPERATION, "订单不能支付");
        }

        WxPayMwebOrderResult result = null;
        try {
            WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
            orderRequest.setOutTradeNo(order.getOrderSn());
            orderRequest.setTradeType("MWEB");
            orderRequest.setBody("订单：" + order.getOrderSn());
            // 元转成分
            int fee = 0;
            BigDecimal actualPrice = order.getActualPrice();
            fee = actualPrice.multiply(new BigDecimal(100)).intValue();
            orderRequest.setTotalFee(fee);
            orderRequest.setSpbillCreateIp(IpUtil.getIpAddr(request));

            result = wxPayService.createOrder(orderRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseUtil.ok(result);
    }

    /**
     * 微信付款成功或失败回调接口
     * 1. 检测当前订单是否是付款状态;
     * 2. 设置订单付款成功状态相关信息;
     * 3. 响应微信商户平台.
     */
    @Transactional
    public String payNotify(String bodyXml) {
        WxPayOrderNotifyResult result = null;
        try {
            result = wxPayService.parseOrderNotifyResult(bodyXml);
            if(!WxPayConstants.ResultCode.SUCCESS.equals(result.getResultCode())){
                log.error("订单支付微信回调失败");
                return WxPayNotifyResponse.fail("失败");
            }
            if(!WxPayConstants.ResultCode.SUCCESS.equals(result.getReturnCode())){
                log.error("订单支付微信回调失败");
                return WxPayNotifyResponse.fail("失败");
            }
        } catch (WxPayException e) {
            log.error("订单支付微信回调失败", e);
            return WxPayNotifyResponse.fail("失败");
        }

        logger.info("处理腾讯支付平台的订单支付");
        logger.info(result);

        String orderSn = result.getOutTradeNo();
        String payId = result.getTransactionId();

        // 分转化成元
        String totalFee = BaseWxPayResult.fenToYuan(result.getTotalFee());
        LitemallOrder order = orderService.findBySn(orderSn);
        if (order == null) {
            return WxPayNotifyResponse.fail("订单不存在 sn=" + orderSn);
        }

        // 检查这个订单是否已经处理过
        if (OrderUtil.hasPayed(order)) {
            return WxPayNotifyResponse.success("订单已经处理成功!");
        }

        // 检查支付订单金额
        if (!totalFee.equals(order.getActualPrice().toString())) {
            return WxPayNotifyResponse.fail(order.getOrderSn() + " : 支付金额不符合 totalFee=" + totalFee);
        }

        order.setPayId(payId);
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(OrderUtil.STATUS_PAY);
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            return WxPayNotifyResponse.fail("更新数据已失效");
        }

        // 取消订单超时未支付任务
        taskService.removeTask(new OrderUnpaidTask(order.getId()));

        return WxPayNotifyResponse.success("成功");
    }

    /**
     * 订单申请退款
     * <p>
     * 1. 检测当前订单是否能够退款；
     * 2. 设置订单申请退款状态。
     *
     * @param userId 用户ID
     * @param body   订单信息，{ orderId：xxx }
     * @return 订单退款操作结果
     */
    public Object refund(Integer userId, String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.badArgumentValue();
        }

        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isRefund()) {
            return ResponseUtil.fail(ORDER_INVALID_OPERATION, "订单不能取消");
        }

        // 设置订单申请退款状态
        order.setOrderStatus(OrderUtil.STATUS_REFUND);
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            return ResponseUtil.updatedDateExpired();
        }

        //TODO 发送邮件和短信通知，这里采用异步发送
        // 有用户申请退款，邮件通知运营人员
        notifyService.notifyMail("退款申请", order.toString());

        return ResponseUtil.ok();
    }

    /**
     * 确认收货
     * <p>
     * 1. 检测当前订单是否能够确认收货；
     * 2. 设置订单确认收货状态。
     *
     * @param userId 用户ID
     * @param req   订单信息，{ orderId：xxx }
     * @return 订单操作结果
     */
    public Result confirm(Integer userId, OrderReq req) {
        Integer orderId = req.getOrderId();
        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return Result.fail(ErrorCodeEnum.ORDER_NOT_EXIST_ERROR);
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail(ErrorCodeEnum.ORDER_NOT_EXIST_ERROR);
        }

        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isConfirm()) {
            return Result.fail(ErrorCodeEnum.ORDER_COMFIRM_ERROR);
        }

        Short comments = orderGoodsService.getComments(orderId);
        order.setComments(comments);

        order.setOrderStatus(OrderUtil.STATUS_CONFIRM);
        order.setConfirmTime(LocalDateTime.now());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            return Result.fail(ErrorCodeEnum.ORDER_COMFIRM_ERROR);
        }
        return Result.ok();
    }

    /**
     * 删除订单
     * <p>
     * 1. 检测当前订单是否可以删除；
     * 2. 删除订单。
     *
     * @param userId 用户ID
     * @param req   订单信息，{ orderId：xxx }
     * @return 订单操作结果
     */
    public Result delete(Integer userId, OrderReq req) {
        Integer orderId = req.getOrderId();
        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return Result.fail(ErrorCodeEnum.ORDER_NOT_EXIST_ERROR);
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail(ErrorCodeEnum.ORDER_NOT_EXIST_ERROR);
        }

        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isDelete()) {
            return Result.fail(ErrorCodeEnum.ORDER_CAN_NOT_DELETE_ERROR);
        }

        // 订单order_status没有字段用于标识删除
        // 而是存在专门的delete字段表示是否删除
        orderService.deleteById(orderId);

        return Result.ok();
    }

    /**
     * 待评价订单商品信息
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @param goodsId 商品ID
     * @return 待评价订单商品信息
     */
    public Object goods(Integer userId, Integer orderId, Integer goodsId) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        List<LitemallOrderGoods> orderGoodsList = orderGoodsService.findByOidAndGid(orderId, goodsId);
        int size = orderGoodsList.size();

        Assert.state(size < 2, "存在多个符合条件的订单商品");

        if (size == 0) {
            return ResponseUtil.badArgumentValue();
        }

        LitemallOrderGoods orderGoods = orderGoodsList.get(0);
        return ResponseUtil.ok(orderGoods);
    }

    /**
     * 评价订单商品
     * <p>
     * 确认商品收货或者系统自动确认商品收货后7天内可以评价，过期不能评价。
     *
     * @param userId 用户ID
     * @param body   订单信息，{ orderId：xxx }
     * @return 订单操作结果
     */
    public Object comment(Integer userId, String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        Integer orderGoodsId = JacksonUtil.parseInteger(body, "orderGoodsId");
        if (orderGoodsId == null) {
            return ResponseUtil.badArgument();
        }
        LitemallOrderGoods orderGoods = orderGoodsService.findById(orderGoodsId);
        if (orderGoods == null) {
            return ResponseUtil.badArgumentValue();
        }
        Integer orderId = orderGoods.getOrderId();
        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgumentValue();
        }
        Short orderStatus = order.getOrderStatus();
        if (!OrderUtil.isConfirmStatus(order) && !OrderUtil.isAutoConfirmStatus(order)) {
            return ResponseUtil.fail(ORDER_INVALID_OPERATION, "当前商品不能评价");
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.fail(ORDER_INVALID, "当前商品不属于用户");
        }
        Integer commentId = orderGoods.getComment();
        if (commentId == -1) {
            return ResponseUtil.fail(ORDER_COMMENT_EXPIRED, "当前商品评价时间已经过期");
        }
        if (commentId != 0) {
            return ResponseUtil.fail(ORDER_COMMENTED, "订单商品已评价");
        }

        String content = JacksonUtil.parseString(body, "content");
        Integer star = JacksonUtil.parseInteger(body, "star");
        if (star == null || star < 0 || star > 5) {
            return ResponseUtil.badArgumentValue();
        }
        Boolean hasPicture = JacksonUtil.parseBoolean(body, "hasPicture");
        List<String> picUrls = JacksonUtil.parseStringList(body, "picUrls");
        if (hasPicture == null || !hasPicture) {
            picUrls = new ArrayList<>(0);
        }

        // 1. 创建评价
        LitemallComment comment = new LitemallComment();
        comment.setUserId(userId);
        comment.setType((byte) 0);
        comment.setValueId(orderGoods.getGoodsId());
        comment.setStar(star.shortValue());
        comment.setContent(content);
        comment.setHasPicture(hasPicture);
        comment.setPicUrls(picUrls.toArray(new String[]{}));
        commentService.save(comment);

        // 2. 更新订单商品的评价列表
        orderGoods.setComment(comment.getId());
        orderGoodsService.updateById(orderGoods);

        // 3. 更新订单中未评价的订单商品可评价数量
        Short commentCount = order.getComments();
        if (commentCount > 0) {
            commentCount--;
        }
        order.setComments(commentCount);
        orderService.updateWithOptimisticLocker(order);

        return ResponseUtil.ok();
    }

}