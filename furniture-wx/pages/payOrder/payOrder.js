var util = require('../../utils/util.js');
var api = require('../../config/api.js');
// pages/payOrder/payOrder.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    status: 1,
    orderId: '',
    showPayPwdInput: false, //是否展示密码输入层
    actualPrice: '',
    showactualPrice: '',
    pwdVal: '', //输入的密码
    payFocus: true, //文本框焦点
    payType: 1,
    payTypeStauts: 1,
    balance: '0.00',
    openid: '',
    isSetPassword: 1,
    info: {},
    canPay: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    this.setData({
      status: Number(options.status),
      orderId: options.orderId,
      actualPrice: options.actualPrice.replace(/,/g, ''),
      showactualPrice: util.fmoney(options.actualPrice, 2)
    });
    this.init();
  },
  walletPay: function () {
    if (this.data.isSetPassword !== 1) {
      wx.showModal({
        title: '',
        content: '您未设置支付密码,是否前去设置?',
        success: function (res) {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/ucenter/setPasswd/setPasswd'
            })
          }
        }
      });
      return;
    }
    if (!this.data.canPay) {
      return;
    }
    this.showInputLayer();
    this.setData({
      payType: 1,
    })
  },
  getUserInfo: function (e) {
    wx.login({
      success: (ret) => {
        if (ret.code) {
          util.request(api.autoWechat, {
            code: ret.code
          }, 'post').then((res) => {
            if (res.success) {
              this.setData({
                openid: res.data.openid
              });
              this.setData({
                payType: 2,
              })
              this.orderPay();
            }
          });
        }
      }
    })
  },
  init: function () {
    util.request(api.walletInfo, {}, 'post').then((res) => {
      if (res.success) {
        this.setData({
          balance: util.fmoney(res.data.balance, 2),
          openid: res.data.openid,
          isSetPassword: res.data.isSetPassword,
          balance: util.fmoney(res.data.balance, 2),
          canPay: Number(res.data.balance) >= Number(this.data.actualPrice)
        });
        console.log(Number(res.data.balance), this.data.actualPrice)
      }
    });
  },
  /**
   * 显示支付密码输入层
   */
  showInputLayer: function () {
    this.setData({
      showPayPwdInput: true,
      payFocus: true
    });
  },
  /**
   * 隐藏支付密码输入层
   */
  hidePayLayer: function () {
    /**在这调用支付接口**/
    this.setData({
      showPayPwdInput: false,
      payFocus: false,
      pwdVal: ''
    });

  },
  forGot() {
    wx.navigateTo({
      url: '/pages/ucenter/setPasswd/setPasswd'
    })
  },
  /**
   * 获取焦点
   */
  getFocus: function () {
    this.setData({
      payFocus: true
    });
  },
  /**
   * 输入密码监听
   */
  inputPwd: function (e) {
    this.setData({
      pwdVal: e.detail.value
    });
    console.log(e.detail.value)
    if (e.detail.value.length === 6) {
      this.orderPay()
    }
  },

  orderPay: function () {
    if (!this.data.canPay) {
      wx.showModal({
        title: '错误信息',
        content: '钱包余额不足',
        showCancel: false
      });
      return;
    }
    const that = this;
    const OrderApi = this.data.status === 1 ? api.OrderPrepay : api.OrderFinalpay;
    util.request(OrderApi, {
      orderId: this.data.orderId,
      openid: this.data.info.openid || '',
      password: this.data.pwdVal,
      payType: this.data.payType,
    }, 'POST').then((res) => {
      if (!res.success) {
        if (res.code !== '1000') {
          wx.showToast({
            title: res.msg,
            icon: 'none'
          })
          this.setData({
            pwdVal: '',
            payFocus: true
          });
        }
        return;
      }
      if (res.success) {
        that.hidePayLayer();
        if (that.data.payType === 1) {
          that.setData({
            showPayPwdInput: false,
            payFocus: false,
            pwdVal: ''
          }, function () {
            wx.redirectTo({
              url: `/pages/payResult/payResult?status=1&orderId=${that.data.orderId}&actualPrice=${that.data.actualPrice}`
            });
          });
          return;
        }
        if (that.data.payType === 2) {
          const payParam = res.data;
          // 微信支付
          wx.requestPayment({
            'timeStamp': payParam.timeStamp,
            'nonceStr': payParam.nonceStr,
            'package': payParam.packageValue,
            'signType': payParam.signType,
            'paySign': payParam.paySign,
            'success': function (res) {
              wx.redirectTo({
                url: `/pages/payResult/payResult?status=1&orderId=${that.data.orderId}&actualPrice=${that.data.actualPrice}`
              });
            },
            'fail': function (res) {
              console.log("支付过程失败");
              wx.redirectTo({
                url: `/pages/payResult/payResult?status=0&orderId=${that.data.orderId}&actualPrice=${that.data.actualPrice}`
              });
            },
            'complete': function (res) {
              console.log("支付过程结束")
            }
          });
        }
      } else {
        wx.redirectTo({
          url: `/pages/payResult/payResult?status=0&orderId=${that.data.orderId}&actualPrice=${that.data.actualPrice}`
        });
      }
    });
  },
  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {

  },
})