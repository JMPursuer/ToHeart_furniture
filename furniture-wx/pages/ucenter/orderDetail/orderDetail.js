var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');

Page({
  data: {
    orderId: 0,
    orderInfo: {},
    orderGoods: [],
    expressInfo: {},
    flag: false,
    handleOption: {},
    orderClass: {
      101: {
        txt: '等待买家付订金',
        cls: 'order-pay'
      },
      301: {
        txt: '等待买家确认收货',
        cls: 'order-home'
      },
      201: {
        txt: '等待商家确认订金',
        cls: 'order-home'
      },
      202: {
        txt: '等待买家付尾款',
        cls: 'order-pay'
      },
      203: {
        txt: '等待卖家发货',
        cls: 'order-ship'
      },
      204: {
        txt: '订单已取消，退款中',
        cls: 'order-close'
      },
      205: {
        txt: '已退款',
        cls: 'order-close'
      },
      401: {
        txt: '交易完成',
        cls: 'order-finish'
      },
      402: {
        txt: '交易完成',
        cls: 'order-finish'
      }
    },
  },
  onLoad: function (options) {
    // 页面初始化 options为页面跳转所带来的参数
    this.setData({
      orderId: options.id
    });
    this.getOrderDetail();
  },
  onPullDownRefresh() {
    wx.showNavigationBarLoading() //在标题栏中显示加载
    this.getOrderDetail();
    wx.hideNavigationBarLoading() //完成停止加载
    wx.stopPullDownRefresh() //停止下拉刷新
  },
  expandDetail: function () {
    let that = this;
    this.setData({
      flag: !that.data.flag
    })
  },
  getOrderDetail: function () {
    wx.showLoading({
      title: '加载中',
    });

    setTimeout(function () {
      wx.hideLoading()
    }, 2000);

    let that = this;
    util.request(api.OrderDetail, {
      orderId: that.data.orderId
    }, 'POST').then(function (res) {
      if (res.success) {
        res.data.orderInfo.dueFinalpay = util.fmoney(res.data.orderInfo.dueFinalpay, 2);
        res.data.orderInfo.finalpayPrice = util.fmoney(res.data.orderInfo.finalpayPrice, 2);
        res.data.orderInfo.actualPrice = util.fmoney(res.data.orderInfo.actualPrice, 2);
        res.data.orderInfo.goodsPrice = util.fmoney(res.data.orderInfo.goodsPrice, 2);
        res.data.orderInfo.oweFinalpay = util.fmoney(res.data.orderInfo.oweFinalpay, 2);
        res.data.orderInfo.paidFinalpay = util.fmoney(res.data.orderInfo.paidFinalpay, 2);
        res.data.orderInfo.prepayPrice = util.fmoney(res.data.orderInfo.prepayPrice, 2);
        res.data.orderGoods.forEach(ret=>{
          ret.progress = Number(((1 / ret.number) * 100).toFixed(0));
        })
        console.log(res.data.orderGoods)
        that.setData({
          orderInfo: res.data.orderInfo,
          orderGoods: res.data.orderGoods,
          handleOption: res.data.orderInfo.handleOption
          // expressInfo: res.data.expressInfo
        });
      }

      wx.hideLoading();
    });
  },
  // “去付款”按钮点击效果
  payOrder: function () {
    const orderId = this.data.orderId;
    const status = this.data.orderInfo.orderStatus === 101 ? 1 : this.data.orderInfo.orderStatus === 202 ? 2 : this.data.orderInfo.orderStatus === 206 ? 3 : 0;
    const actualPrice = this.data.orderInfo.orderStatus === 101 ? this.data.orderInfo.prepayPrice : this.data.orderInfo.orderStatus === 202 ?
      this.data.orderInfo.finalpayPrice : this.data.orderInfo.orderStatus === 206?this.data.orderInfo.dueFinalpay:'';
    wx.redirectTo({
      url: `/pages/payOrder/payOrder?status=${status}&orderId=${orderId}&actualPrice=${actualPrice}`
    });
  },
  // “取消订单”点击效果
  cancelOrder: function () {
    let that = this;
    let orderInfo = that.data.orderInfo;

    wx.showModal({
      title: '',
      content: '确定要取消此订单？',
      success: function (res) {
        if (res.confirm) {
          util.request(api.OrderCancel, {
            orderId: orderInfo.id
          }, 'POST').then(function (res) {
            if (res.success) {
              wx.showToast({
                title: '取消订单成功'
              });
              wx.navigateBack();
            } else {
              util.showErrorToast(res.errmsg);
            }
          });
        }
      }
    });
  },
  // “取消订单并退款”点击效果
  refundOrder: function () {
    let that = this;
    let orderInfo = that.data.orderInfo;

    wx.showModal({
      title: '',
      content: '确定要取消此订单？',
      success: function (res) {
        if (res.confirm) {
          util.request(api.OrderRefund, {
            orderId: orderInfo.id
          }, 'POST').then(function (res) {
            if (res.success) {
              wx.showToast({
                title: '取消订单成功',
                icon:'none'
              });
              setTimeout(()=>{
                wx.navigateBack()
              },10000)
            } else {
              util.showErrorToast(res.errmsg);
            }
          });
        }
      }
    });
  },
  // “删除”点击效果
  deleteOrder: function () {
    let that = this;
    let orderInfo = that.data.orderInfo;

    wx.showModal({
      title: '',
      content: '确定要删除此订单？',
      success: function (res) {
        if (res.confirm) {
          util.request(api.OrderDelete, {
            orderId: orderInfo.id
          }, 'POST').then(function (res) {
            if (res.success) {
              wx.showToast({
                title: '删除订单成功'
              });
              util.redirect('/pages/ucenter/order/order');
            } else {
              util.showErrorToast(res.errmsg);
            }
          });
        }
      }
    });
  },
  // “确认收货”点击效果
  confirmOrder: function () {
    let that = this;
    let orderInfo = that.data.orderInfo;

    wx.showModal({
      title: '',
      content: '确认收货？',
      success: function (res) {
        if (res.confirm) {
          util.request(api.OrderConfirm, {
            orderId: orderInfo.id
          }, 'POST').then(function (res) {
            if (res.success) {
              wx.showToast({
                title: '确认收货成功！'
              });
              util.redirect('/pages/ucenter/order/order');
            } else {
              util.showErrorToast(res.errmsg);
            }
          });
        }
      }
    });
  },
  copySn: function (e) {
    let value = e.currentTarget.dataset['sn'];
    wx.setClipboardData({
      //准备复制的数据
      data: value,
      success: function (res) {
        wx.showToast({
          title: '复制成功',
        });
      }
    });
  },
  showState: function (state) {
    console.log(state)
  },
  onReady: function () {
    // 页面渲染完成
  },
  onShow: function () {
    // 页面显示
  },
  onHide: function () {
    // 页面隐藏
  },
  onUnload: function () {
    // 页面关闭
  }
})