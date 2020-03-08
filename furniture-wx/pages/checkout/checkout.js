var util = require('../../utils/util.js');
var api = require('../../config/api.js');

var app = getApp();

Page({
  data: {
    checkedGoodsList: [],
    checkedAddress: {},
    isTax: 0,
    order: {},
    availableCouponLength: 0, // 可用的优惠券数量
    goodsTotalPrice: 0.00, //商品总价
    freightPrice: 0.00, //快递费
    couponPrice: 0.00, //优惠券的价格
    grouponPrice: 0.00, //团购优惠价格
    orderTotalPrice: 0.00, //订单总价
    actualPrice: 0.00, //实际需要支付的总价
    cartId: 0,
    addressId: 0,
    couponId: 0,
    userCouponId: 0,
    message: '',
    grouponLinkId: 0, //参与的团购
    grouponRulesId: 0 //团购规则ID
  },
  onLoad: function (options) {

    var cartId = 0;
    if (options.cartId) {
      cartId = Number(options.cartId);
    } else {
      cartId = wx.getStorageSync('cartId') || 0;
    }
    this.setData({
      buyType: Number(options.buyType) || 1,
      cartId:cartId
    })
    // 页面初始化 options为页面跳转所带来的参数
  },

  //获取checkou信息
  getCheckoutInfo: function () {
    let that = this;
    util.request(api.CartCheckout, {
      cartId: that.data.cartId,
      addressId: that.data.addressId
    }).then(function (res) {
      if (res.success) {
        const order = {
          goodsTotalPrice: res.data.goodsTotalPrice,
          orderTotalPrice: res.data.orderTotalPrice,
          actualPrice: res.data.actualPrice,
          freightPrice: res.data.freightPrice,
          taxPrice: res.data.taxPrice,
          discountPrice: Number(res.data.discountPrice).toFixed(2)
        }
        that.setData({
          order,
          actualPrice: res.data.actualPrice,
          checkedGoodsList: res.data.checkedGoodsList,
          checkedAddress: res.data.checkedAddress,
          addressId: res.data.addressId,
        });
      }
      wx.hideLoading();
    });
  },
  selectAddress() {
    wx.navigateTo({
      url: '/pages/ucenter/address/address',
    })
  },
  selectCoupon() {
    wx.navigateTo({
      url: '/pages/ucenter/couponSelect/couponSelect',
    })
  },
  bindMessageInput: function (e) {
    this.setData({
      message: e.detail.value
    });
  },
  onReady: function () {
    // 页面渲染完成

  },
  onShow: function (options) {
    // 页面显示
    wx.showLoading({
      title: '加载中...',
    });
    try {
      var addressId = wx.getStorageSync('addressId');
      if (addressId === "") {
        addressId = 0;
      }
      this.setData({
        addressId: addressId,
      });

    } catch (e) {
      // Do something when catch error
      console.log(e);
    }

    this.getCheckoutInfo();
  },
  onHide: function () {
    // 页面隐藏

  },
  onUnload: function () {
    // 页面关闭

  },
  isTaxChange: function (e) {
    let isTax = 0;
    let actualPrice = this.data.order.actualPrice;
    if (e.detail.value.length > 0) {
      isTax = Number(e.detail.value[0])
    }
    if (isTax === 1) {
      actualPrice = this.data.order.actualPrice + this.data.order.taxPrice
    }
    actualPrice = actualPrice.toFixed(2);
    this.setData({
      isTax: isTax,
      actualPrice: actualPrice
    });
  },
  submitOrder: function () {
    if (this.data.addressId <= 0) {
      util.showErrorToast('请选择收货地址');
      return false;
    }
    util.request(api.OrderSubmit, {
      cartId: this.data.cartId,
      buyType: this.data.buyType,
      addressId: this.data.addressId,
      message: this.data.message,
      isTax: this.data.isTax
    }, 'POST').then(res => {
      if (res.success) {
        const orderId = res.data.orderId;
        wx.redirectTo({
          url: `/pages/payOrder/payOrder?status=1&orderId=${orderId}&actualPrice=${res.data.prepayPrice}`
        });
      } else {
        util.showErrorToast(res.errmsg);
      }
    });
  }
});