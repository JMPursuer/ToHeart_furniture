package com.xinzhi.furniture.mall.db.service;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.google.common.collect.Lists;
import com.xinzhi.furniture.mall.db.dao.*;
import com.xinzhi.furniture.mall.db.domain.*;
import com.xinzhi.furniture.mall.db.enumeration.FlowTypeEnum;
import com.xinzhi.furniture.mall.db.enumeration.TopUpStatusEnum;
import com.xinzhi.furniture.mall.db.enumeration.UserStatusEnum;
import com.xinzhi.furniture.mall.db.result.ErrorCodeEnum;
import com.xinzhi.furniture.mall.db.result.PageData;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.req.Page;
import com.xinzhi.furniture.mall.db.service.req.wallet.AdminTopUpReq;
import com.xinzhi.furniture.mall.db.service.req.wallet.AdminWithdrawReq;
import com.xinzhi.furniture.mall.db.service.req.wallet.TopUpReq;
import com.xinzhi.furniture.mall.db.service.req.wallet.WalletListReq;
import com.xinzhi.furniture.mall.db.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xinzhi.furniture.mall.db.service.resp.wallet.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
@Slf4j
public class WalletService {

    @Autowired
    private LitemallWalletMapper litemallWalletMapper;

    @Resource
    private LitemallWalletFlowMapper litemallWalletFlowMapper;

    @Autowired
    private WxPayService wxPayService;

    @Autowired
    private LitemallOrderService litemallOrderService;

    @Resource
    private LitemallWalletTopUpMapper litemallWalletTopUpMapper;

    @Autowired
    private UserWalletMapper walletMapper;

    @Autowired
    private LitemallUserMapper userMapper;


    public Result<WalletResp> getBalance(Integer userId) {
        LitemallUser user = userMapper.selectByPrimaryKey(userId);
        LitemallWallet wallet = litemallWalletMapper.selectOneByExample(LitemallWalletExample.newAndCreateCriteria().andUserIdEqualTo(userId).example());
        WalletResp walletResp = new WalletResp();
        walletResp.setBalance(wallet.getBalance());
        walletResp.setOpenid(user.getWeixinOpenid());
        return Result.ok().setResult(walletResp);
    }


    public Result<PageData<WalletFlowResp>> getWalletFlowList(Integer userId, Page req) {
        LitemallWalletFlowExample example = new LitemallWalletFlowExample();
        example.createCriteria().andUserIdEqualTo(userId);
        long count = litemallWalletFlowMapper.countByExample(example);

        example.setOrderByClause(LitemallUser.Column.id.name() + " " + Constant.ORDER_DESC + " " + Constant.LIMIT + " " + (req.getPageIndex() - 1) * req.getPageSize() + "," + req.getPageSize());
        List<LitemallWalletFlow> litemallWalletFlowList = litemallWalletFlowMapper.selectByExample(example);
        List<WalletFlowResp> list = Lists.newArrayList();
        for (LitemallWalletFlow walletFlow : litemallWalletFlowList) {
            WalletFlowResp resp = new WalletFlowResp();
            BeanUtils.copyProperties(walletFlow, resp);
            list.add(resp);
        }
        PageData<WalletFlowResp> pageData = new PageData<>();
        pageData.setPageIndex(req.getPageIndex());
        pageData.setPageSize(req.getPageSize());
        pageData.setTotal(count);
        pageData.setList(list);
        return Result.ok().setResult(pageData);
    }

    public Result<WxPayMpOrderResult> topUp(Integer userId, TopUpReq req, String ip) {
        String topupSn = litemallOrderService.generateOrderSn(userId);
        // 第一步提交充值订单
        LitemallWalletTopUp topUp = new LitemallWalletTopUp();
        topUp.setAmount(req.getAmount());
        topUp.setCreateTime(Instant.now().toEpochMilli());
        topUp.setStatus(TopUpStatusEnum.SUBMIT.getCode());
        topUp.setTopupSn(topupSn);
        topUp.setUserId(userId);
        litemallWalletTopUpMapper.insertSelective(topUp);

        WxPayMpOrderResult result = null;
        try {
            WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
            orderRequest.setNotifyUrl(wxPayService.getConfig().getNotifyUrl() + "/wx/wallet/notify");
            orderRequest.setOutTradeNo(topupSn);
            orderRequest.setOpenid(req.getOpenid());
            orderRequest.setBody("钱包充值");
            // 元转成分
            int fee = req.getAmount().multiply(new BigDecimal(100)).intValue();
            orderRequest.setTotalFee(fee);
            orderRequest.setSpbillCreateIp(ip);
            result = wxPayService.createOrder(orderRequest);
        } catch (Exception e) {
            log.error("充值失败", e);
            throw new RuntimeException("钱包充值失败");
        }
        return Result.ok().setResult(result);
    }

    public String payNotify(String bodyXml) {
        try {
            WxPayOrderNotifyResult wxPayOrderNotifyResult = wxPayService.parseOrderNotifyResult(bodyXml);
            if(!"SUCCESS".equals(wxPayOrderNotifyResult.getResultCode())) {
                return WxPayNotifyResponse.fail("失败");
            }
            String outTradeNo = wxPayOrderNotifyResult.getOutTradeNo();
            LitemallWalletTopUp walletTopUp = litemallWalletTopUpMapper.selectOneByExampleSelective(LitemallWalletTopUpExample.newAndCreateCriteria().andTopupSnEqualTo(outTradeNo).example());
            if(walletTopUp == null || walletTopUp.getStatus() == TopUpStatusEnum.PAID_FAIL.getCode()) {
                return WxPayNotifyResponse.fail("失败");
            }
            if(walletTopUp.getStatus() == TopUpStatusEnum.PAID_SUCCESS.getCode()) {
                return WxPayNotifyResponse.success("成功");
            }
            // 修改充值订单状态
            walletTopUp.setPayId(wxPayOrderNotifyResult.getTransactionId());
            walletTopUp.setStatus(TopUpStatusEnum.PAID_SUCCESS.getCode());
            walletTopUp.setUpdateTime(Instant.now().toEpochMilli());
            litemallWalletTopUpMapper.updateByPrimaryKeySelective(walletTopUp);

            Integer userId = walletTopUp.getUserId();
            LitemallWallet wallet = litemallWalletMapper.selectOneByExampleSelective(LitemallWalletExample.newAndCreateCriteria().andUserIdEqualTo(userId).example());
            // 添加流水记录
            LitemallWalletFlow walletFlow = new LitemallWalletFlow();
            walletFlow.setBalance(wallet.getBalance().add(walletTopUp.getAmount()));
            walletFlow.setCreateTime(Instant.now().toEpochMilli());
            walletFlow.setDesc("微信充值");
            walletFlow.setFlow(walletTopUp.getAmount());
            walletFlow.setUserId(userId);
            walletFlow.setOrderSn(walletTopUp.getTopupSn());
            walletFlow.setType(FlowTypeEnum.WECHAT_TOP_UP.getCode());
            litemallWalletFlowMapper.insertSelective(walletFlow);

            // 增加钱包余额
            walletMapper.increaseBalance(userId, walletTopUp.getAmount());

            return WxPayNotifyResponse.success("成功");
        } catch (WxPayException e) {
            log.error("微信支付回调失败", e);
        }
        return WxPayNotifyResponse.fail("失败");
    }

    public Result<PageData<WalletListResp>> getWalletList(WalletListReq req) {
        LitemallWalletExample example = new LitemallWalletExample();
        if(!StringUtils.isEmpty(req.getMobile())) {
            LitemallUser user = userMapper.selectOneByExampleSelective(LitemallUserExample.newAndCreateCriteria().andMobileEqualTo(req.getMobile()).example());
            if(user == null) {
                return Result.fail(ErrorCodeEnum.PHONE_NOT_EXIST_ERROR);
            }
            example.createCriteria().andUserIdEqualTo(user.getId());
        }
        long count = litemallWalletMapper.countByExample(example);

        example.setOrderByClause(LitemallWallet.Column.id.desc() + " " + Constant.LIMIT + " " + (req.getPageIndex() - 1) * req.getPageSize() + "," + req.getPageSize());
        List<LitemallWallet> litemallWalletList = litemallWalletMapper.selectByExampleSelective(example);
        List<WalletListResp> walletListRespList = Lists.newArrayList();
        for(LitemallWallet wallet : litemallWalletList) {
            Integer userId = wallet.getUserId();
            LitemallUser litemallUser = userMapper.selectByPrimaryKey(userId);
            WalletListResp walletListResp = new WalletListResp();
            walletListResp.setBalance(wallet.getBalance());
            walletListResp.setMobile(litemallUser.getMobile());
            walletListResp.setUserId(litemallUser.getId());
            walletListResp.setNickname(litemallUser.getNickname());
            walletListRespList.add(walletListResp);
        }

        PageData<WalletListResp> pageData = new PageData<>();
        pageData.setPageIndex(req.getPageIndex());
        pageData.setPageSize(req.getPageSize());
        pageData.setTotal(count);
        pageData.setList(walletListRespList);
        return Result.ok().setResult(pageData);
    }

    public Result adminTopUp(AdminTopUpReq req, String optName) {
        if(req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail(ErrorCodeEnum.INPUT_ERROR);
        }
        LitemallUser user = userMapper.selectByPrimaryKey(req.getUserId());
        if(user == null) {
            return Result.fail(ErrorCodeEnum.USER_NOT_EXIST);
        }
        if(user.getStatus() != UserStatusEnum.OK.getCode().byteValue()) {
            return Result.fail(ErrorCodeEnum.USER_STATUS_ERROR);
        }

        LitemallWallet wallet = litemallWalletMapper.selectOneByExampleSelective(LitemallWalletExample.newAndCreateCriteria().andUserIdEqualTo(req.getUserId()).example());
        // 添加流水记录
        LitemallWalletFlow walletFlow = new LitemallWalletFlow();
        walletFlow.setBalance(wallet.getBalance().add(req.getAmount()));
        walletFlow.setCreateTime(Instant.now().toEpochMilli());
        walletFlow.setDesc("充值");
        walletFlow.setFlow(req.getAmount());
        walletFlow.setUserId(req.getUserId());
        walletFlow.setType(FlowTypeEnum.ADMIN_TOP_UP.getCode());
        walletFlow.setOptName(optName);
        walletFlow.setNote(req.getNote());
        litemallWalletFlowMapper.insertSelective(walletFlow);

        walletMapper.increaseBalance(req.getUserId(), req.getAmount());
        return Result.ok();
    }

    public Result adminWithdraw(AdminWithdrawReq req, String optName) {
        if(req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail(ErrorCodeEnum.INPUT_ERROR);
        }
        LitemallUser user = userMapper.selectByPrimaryKey(req.getUserId());
        if(user == null) {
            return Result.fail(ErrorCodeEnum.USER_NOT_EXIST);
        }
        if(user.getStatus() != UserStatusEnum.OK.getCode().byteValue()) {
            return Result.fail(ErrorCodeEnum.USER_STATUS_ERROR);
        }

        LitemallWallet wallet = litemallWalletMapper.selectOneByExampleSelective(LitemallWalletExample.newAndCreateCriteria().andUserIdEqualTo(req.getUserId()).example());
        // 添加流水记录
        BigDecimal amount = req.getAmount().multiply(new BigDecimal(-1));

        LitemallWalletFlow walletFlow = new LitemallWalletFlow();
        walletFlow.setBalance(wallet.getBalance().add(amount));
        walletFlow.setCreateTime(Instant.now().toEpochMilli());
        walletFlow.setDesc("提现");
        walletFlow.setFlow(amount);
        walletFlow.setUserId(req.getUserId());
        walletFlow.setType(FlowTypeEnum.ADMIN_WITHDRAW.getCode());
        walletFlow.setOptName(optName);
        walletFlow.setNote(req.getNote());
        litemallWalletFlowMapper.insertSelective(walletFlow);

        walletMapper.increaseBalance(req.getUserId(), amount);
        return Result.ok();
    }


}
