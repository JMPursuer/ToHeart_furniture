// pages/ucenter/topup/topup.js
var util = require("../../../utils/util.js");
var api = require("../../../config/api.js");
Page({
  /**
   * 页面的初始数据
   */
  data: {
    balance: "",
    openid: "",
    amount: ""
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {},
  bindAmountInput: function (e) {
    this.setData({
      amount: e.detail.value
    });
  },
  topup() {
    if (Number(this.data.amount) <= 0) {
      util.showErrorToast("充值金额不能小于0元");
      return;
    }
    if (this.data.amount > 3000) {
      wx.showModal({
        title: "充值提醒",
        content: "单次最多只能充值3000元,若需要充值更多，请联系上级销售人员",
        showCancel: false
      });
      return;
    }
    util
      .request(
        api.walletTopup, {
          amount: this.data.amount,
          openid: this.data.openid
        },
        "post"
      )
      .then(res => {
        if (!res.success) {
          wx.showToast({
            title: res.msg,
            icon: "none"
          });
          return;
        }
        const payParam = res.data;
        // 微信支付
        wx.requestPayment({
          timeStamp: payParam.timeStamp,
          nonceStr: payParam.nonceStr,
          package: payParam.packageValue,
          signType: payParam.signType,
          paySign: payParam.paySign,
          success: function (res) {
            wx.showToast({
              title: "充值成功",
              icon: "none"
            });
            wx.navigateBack();
          },
          fail: function (res) {
            console.log("支付失败", res);
            wx.showToast({
              title: "支付失败",
              icon: "none"
            });
          },
          complete: function (res) {
            console.log("支付过程结束");
          }
        });
      });
  },
  getUserInfo: function (e) {
    wx.login({
      success: ret => {
        if (ret.code) {
          util
            .request(
              api.autoWechat, {
                code: ret.code
              },
              "post"
            )
            .then(res => {
              if (res.success) {
                this.setData({
                  openid: res.data.openid
                });
                this.topup();
              }
            });
        }
      }
    });
  },
  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {},

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {
    this.init();
  },
  init: function () {
    util.request(api.walletInfo, {}, "post").then(res => {
      if (res.success) {
        this.setData({
          balance: util.fmoney(res.data.balance, 2),
          openid: res.data.openid
        });
      }
    });
  },
  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {},

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {},

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {},

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {},

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {}
});