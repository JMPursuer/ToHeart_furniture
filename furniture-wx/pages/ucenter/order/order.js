var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');

Page({
  data: {
    orderList: [],
    showType: 0,
    page: 1,
    limit: 10,
    totalPages: 1
  },
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
    let that = this
    try {
      var tab = wx.getStorageSync('tab');

      this.setData({
        showType: tab,
        orderList:[]
      });
    } catch (e) {}

  },
  getOrderList() {
    let that = this;
    util.request(api.OrderList, {
      showType: that.data.showType,
      pageIndex: that.data.page,
      pageSize: that.data.limit
    },'POST').then(function(res) {
      if (res.success) {
        console.log(res.data);
        that.setData({
          orderList: that.data.orderList.concat(res.data.list),
          totalPages: res.data.total
        });
      }
    });
  },
  // “取消订单”点击效果
  cancelOrder: function (e) {
    const orderId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '',
      content: '确定要取消此订单？',
      success: function (res) {
        if (res.confirm) {
          util.request(api.OrderCancel, {
            orderId: orderId
          }, 'POST').then(function (res) {
            if (res.success) {
              wx.showToast({
                title: '取消订单成功'
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
  // “去付款”按钮点击效果
  payOrder: function (e) {
    const orderId = e.currentTarget.dataset.id;
    const orderStatus = e.currentTarget.dataset.status;
    const prepayPrice = e.currentTarget.dataset.price;
    const status = orderStatus === 101 ? 1 : orderStatus === 202 ? 2 : 2;
    const actualPrice = orderStatus === 101 ? prepayPrice : orderStatus === 202 ? e.currentTarget.dataset.finalyPrice : prepayPrice;
    console.log(status, orderId, actualPrice, e)
    wx.redirectTo({
      url: `/pages/payOrder/payOrder?status=${status}&orderId=${orderId}&actualPrice=${actualPrice}`
    });
  },
  // “确认收货”点击效果
  confirmOrder: function (e) {
    let that = this;
    let orderId = e.currentTarget.dataset.id;

    wx.showModal({
      title: '',
      content: '确认收货？',
      success: function (res) {
        if (res.confirm) {
          util.request(api.OrderConfirm, {
            orderId: orderId
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
  onReachBottom() {
    if (this.data.totalPages > this.data.page) {
      this.setData({
        page: this.data.page + 1
      });
      this.getOrderList();
    } else {
      wx.showToast({
        title: '没有更多订单了',
        icon: 'none',
        duration: 2000
      });
      return false;
    }
  },
  switchTab: function(event) {
    let showType = event.currentTarget.dataset.index;
    this.setData({
      orderList: [],
      showType: showType || '0',
      page: 1,
      limit: 10,
      totalPages: 1
    });
    this.getOrderList();
  },
  onReady: function() {
    // 页面渲染完成
  },
  onShow: function() {
    this.setData({
      orderList:[]
    });
    // 页面显示
    this.getOrderList();
  },
  onHide: function() {
    this.setData({
      orderList:[]
    });
    // 页面隐藏
  },
  onUnload: function() {
    // 页面关闭
  }
})