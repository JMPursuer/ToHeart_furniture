var api = require('../../../config/api.js');
var util = require('../../../utils/util.js');
var check = require('../../../utils/check.js');

var app = getApp();
Page({
  data: {
    mobile: '',
    password: '',
    code: '',
    codeName: '发送验证码',
    loginErrorCount: 0,
    type: 1,
  },
  onLoad: function (options) {
    // 页面初始化 options为页面跳转所带来的参数
    // 页面渲染完成

  },
  onReady: function () {

  },
  onShow: function () {
    // 页面显示
  },
  onHide: function () {
    // 页面隐藏

  },
  onUnload: function () {
    // 页面关闭

  },
  countdown() {
    if (typeof this.data.codeName === 'number' && this.data.codeName > 0) {
      this.setData({
        codeName: this.data.codeName - 1
      });
      setTimeout(() => {
        this.countdown();
      }, 1000)
    } else {
      this.setData({
        codeName: '发送验证码'
      });
    }
  },
  /**
   * 短信登录
   */
  checkedLogin() {
    this.setData({
      type: this.data.type === 1 ? 2 : 1,
      code: '',
      password: ''
    });
  },
  smsLogin() {
    const that = this;
    if (this.data.mobile.length == 0) {
      wx.showToast({
        title: '手机号码不能为空',
        icon: 'none',
        duration: 1000
      });
      return false;
    }
    if (this.data.code.length == 0) {
      wx.showToast({
        title: '验证码不能为空',
        icon: 'none',
        duration: 1000
      });
      return false;
    }
    if (!check.isValidPhone(this.data.mobile)) {
      wx.showToast({
        title: '请输入11位数手机号码',
        icon: 'none',
        duration: 1000
      });
      return false;
    }
    wx.request({
      url: api.SmsLogin,
      data: {
        mobile: this.data.mobile,
        code: this.data.code
      },
      method: 'POST',
      header: {
        'content-type': 'application/json'
      },
      success: function (res) {
        if (res.data.success) {
          that.setData({
            loginErrorCount: 0
          });
          app.globalData.hasLogin = true;
          wx.setStorageSync('userInfo', res.data.data);
          wx.setStorageSync('token', res.data.data.token);
          wx.showToast({
            title: '登录成功',
            duration: 1000
          });
          wx.setStorage({
            key: "token",
            data: res.data.data.token,
            success: function () {

              if (res.data.data.status === 3) {
                wx.navigateTo({
                  url: `/pages/auth/reset/reset?mobile=${that.data.mobile}&status=${res.data.data.status}&token=${res.data.data.token}`
                })
                return;
              }
              wx.switchTab({
                url: '/pages/ucenter/index/index'
              });
            }
          });
        } else {
          that.setData({
            loginErrorCount: that.data.loginErrorCount + 1
          });
          app.globalData.hasLogin = false;
          util.showErrorToast(res.data.msg);
        }
      }
    });
  },
  sendCode: function () {
    if (typeof this.data.codeName === 'number') {
      return;
    }
    if (this.data.mobile.length == 0) {
      wx.showToast({
        title: '手机号码不能为空',
        icon: 'none',
        duration: 1000
      });
      return false;
    }

    if (!check.isValidPhone(this.data.mobile)) {
      wx.showToast({
        title: '请输入11位数手机号码',
        icon: 'none',
        duration: 1000
      });
      return false;
    }
    wx.request({
      url: api.SmsCode,
      data: {
        mobile: this.data.mobile
      },
      method: 'POST',
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        if (res.data.success) {
          this.setData({
            codeName: 60
          });
          this.countdown();
          wx.showToast({
            title: '发送成功',
            duration: 1000
          });
        } else {
          wx.showToast({
            title: res.data.msg,
            icon: 'none',
            duration: 1000
          });
        }
      }
    });
  },
  accountLogin: function () {
    var that = this;
    if (!(/^1[3456789]\d{9}$/.test(this.data.mobile))) {
      wx.showToast({
        title: '请输入正确的手机号码',
        icon: 'none',
        duration: 1000
      });
      return false;
    }
    if (this.data.password.length < 1) {
      wx.showToast({
        title: '请输入密码',
        icon: 'none',
        duration: 1000
      });
      return false;
    }

    wx.request({
      url: api.AuthLoginByAccount,
      data: {
        mobile: that.data.mobile,
        password: that.data.password
      },
      method: 'POST',
      header: {
        'content-type': 'application/json'
      },
      success: function (res) {
        if (res.data.success) {
          that.setData({
            loginErrorCount: 0
          });
          app.globalData.hasLogin = true;
          wx.setStorageSync('userInfo', res.data.data);
          wx.setStorageSync('token', res.data.data.token);
          wx.showToast({
            title: '登录成功',
            duration: 1000
          });
          wx.setStorage({
            key: "token",
            data: res.data.data.token,
            success: function () {
              if (res.data.data.status === 3) {
                wx.navigateTo({
                  url: `/pages/auth/reset/reset?mobile=${that.data.mobile}&status=${res.data.data.status}&token=${res.data.data.token}`
                })
                return;
              }
              wx.switchTab({
                url: '/pages/ucenter/index/index'
              });
            }
          });
        } else {
          that.setData({
            loginErrorCount: that.data.loginErrorCount + 1
          });
          app.globalData.hasLogin = false;
          util.showErrorToast(res.data.msg);
        }
      }
    });
  },
  bindUsernameInput: function (e) {

    this.setData({
      mobile: e.detail.value
    });
  },
  bindPasswordInput: function (e) {

    this.setData({
      password: e.detail.value
    });
  },
  bindCodeInput: function (e) {

    this.setData({
      code: e.detail.value
    });
  },
  bindCodeInput: function (e) {

    this.setData({
      code: e.detail.value
    });
  },
  clearInput: function (e) {
    switch (e.currentTarget.id) {
      case 'clear-username':
        this.setData({
          mobile: ''
        });
        break;
      case 'clear-password':
        this.setData({
          password: ''
        });
        break;
      case 'clear-code':
        this.setData({
          code: ''
        });
        break;
    }
  }
})