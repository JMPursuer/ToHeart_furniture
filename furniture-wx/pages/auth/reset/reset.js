const api = require("../../../config/api.js");
const check = require("../../../utils/check.js");
const util = require("../../../utils/util.js");
const app = getApp();
Page({
  data: {
    mobile: "",
    code: "",
    password: "",
    confirmPassword: "",
    status: 0,
    token: "",
    codeName: "发送验证码"
  },
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
    // 页面渲染完成
    console.log(options);
    this.setData({
      mobile: options.mobile,
      status: options.status ? Number(options.status) : 0,
      token: options.token
    });
  },
  onReady: function() {},
  onShow: function() {
    // 页面显示
  },
  onHide: function() {
    // 页面隐藏
  },
  onUnload: function() {
    // 页面关闭
  },
  countdown() {
    if (typeof this.data.codeName === "number" && this.data.codeName > 0) {
      this.setData({
        codeName: this.data.codeName - 1
      });
      setTimeout(() => {
        this.countdown();
      }, 1000);
    } else {
      this.setData({
        codeName: "发送验证码"
      });
    }
  },
  sendCode: function() {
    if (typeof this.data.codeName === "number") {
      return;
    }
    const that = this;
    if (this.data.mobile.length == 0) {
      wx.showToast({
        title: "手机号码不能为空",
        icon: "none",
        duration: 1000
      });
      return false;
    }

    if (!check.isValidPhone(this.data.mobile)) {
      wx.showToast({
        title: "请输入11位数手机号码",
        icon: "none",
        duration: 1000
      });
      return false;
    }
    wx.request({
      url: api.SmsCode,
      data: {
        mobile: that.data.mobile
      },
      method: "POST",
      header: {
        "content-type": "application/json"
      },
      success: function(res) {
        if (res.data.success) {
          that.setData({
            codeName: 60
          });
          that.countdown();
          wx.showToast({
            title: "发送成功",
            duration: 1000
          });
        } else {
          wx.showToast({
            title: res.data.msg,
            icon: "none",
            duration: 1000
          });
        }
      }
    });
  },
  startReset: function() {
    var that = this;
    if (this.data.mobile.length == 0) {
      wx.showToast({
        title: "手机号码不能为空",
        icon: "none",
        duration: 1000
      });
      return false;
    }

    if (!check.isValidPhone(this.data.mobile)) {
      wx.showToast({
        title: "手机号码不正确",
        icon: "none",
        duration: 1000
      });
      return false;
    }

    if (this.data.password.length < 3) {
      wx.showToast({
        title: "用户密码不能少于3位数",
        icon: "none",
        duration: 1000
      });
      return false;
    }

    if (this.data.password != this.data.confirmPassword) {
      wx.showToast({
        title: "确认密码不一致",
        icon: "none",
        duration: 1000
      });
      return false;
    }
    const apiUrl = this.data.status === 3 ? api.AuthReset : api.ForGetPassword;
    const data =
      this.data.status === 3
        ? {
            password: that.data.password,
            repassword: that.data.password
          }
        : {
            code: this.data.code,
            mobile: this.data.mobile,
            password: this.data.password
          };
    util
      .request(apiUrl, data, "POST")
      .then(res => {
        if (res.success) {
          if (that.data.status === 3) {
            setTimeout(() => {
              wx.navigateBack();
            }, 1000);
            wx.showToast({
              title: "修改成功",
              duration: 1000
            });
            return;
          }
          wx.showToast({
            title: "修改成功",
            duration: 1000
          });
          setTimeout(() => {
            wx.switchTab({
              url: "/pages/ucenter/index/index"
            });
          }, 1000);
        } else {
          wx.showModal({
            title: "密码重置失败",
            content: res.msg,
            showCancel: false
          });
        }
      })
      .catch(err => {
        wx.showModal({
          title: "密码重置失败",
          content: err,
          showCancel: false
        });
      });
  },

  bindPasswordInput: function(e) {
    this.setData({
      password: e.detail.value
    });
  },
  bindConfirmPasswordInput: function(e) {
    this.setData({
      confirmPassword: e.detail.value
    });
  },
  bindMobileInput: function(e) {
    this.setData({
      mobile: e.detail.value
    });
  },
  bindCodeInput: function(e) {
    this.setData({
      code: e.detail.value
    });
  },
  clearInput: function(e) {
    switch (e.currentTarget.id) {
      case "clear-password":
        this.setData({
          password: ""
        });
        break;
      case "clear-confirm-password":
        this.setData({
          confirmPassword: ""
        });
        break;
      case "clear-mobile":
        this.setData({
          mobile: ""
        });
        break;
      case "clear-code":
        this.setData({
          code: ""
        });
        break;
    }
  }
});
