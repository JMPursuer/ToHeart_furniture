// pages/ucenter/setPasswd/setPasswd.js
var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');

Page({

  /**
   * 页面的初始数据
   */
  data: {
    pwdVal: '', //输入的密码
    payFocus: true, //文本框焦点
    comfirmPasswd: '',
    type: 1,
    isSetPassword: 1,
    passwordObj: {
      newPassword: '',
      rePassword: '',
      oldPassword: ''
    }
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    this.init();
  },
  getFocus: function () {
    this.setData({
      payFocus: true
    });
  },
  init() {
    let that = this;
    util.request(api.walletInfo, {}, 'post').then(function (res) {
      if (res.success) {
        console.log(res.data.isSetPassword)
        that.setData({
          isSetPassword: res.data.isSetPassword
        });
      }
    });
  },
  /**
   * 输入密码监听
   */
  inputPwd: function (e) {
    this.setData({
      pwdVal: e.detail.value
    });
    if (e.detail.value.length >= 6) {
      let passwordObj = Object.assign({});
      if (this.data.type === 1) {
        if (this.data.isSetPassword === 1) {
          passwordObj = Object.assign({}, this.data.passwordObj, {
            oldPassword: e.detail.value
          })
        } else {
          passwordObj = Object.assign({}, this.data.passwordObj, {
            newPassword: e.detail.value
          })
        }
        this.setData({
          type: 2,
          pwdVal: '',
          payFocus: true,
          passwordObj: passwordObj
        });
      } else if (this.data.type === 2) {
        if (this.data.isSetPassword === 1) {
          passwordObj = Object.assign({}, this.data.passwordObj, {
            newPassword: e.detail.value
          })
          this.setData({
            type: 3,
            pwdVal: '',
            payFocus: true,
            passwordObj: passwordObj
          });
        } else {
          passwordObj = Object.assign({}, this.data.passwordObj, {
            rePassword: e.detail.value
          })
          this.setData({
            passwordObj: passwordObj
          });
          this.setPwd()
        }
      } else {
        passwordObj = Object.assign({}, this.data.passwordObj, {
          rePassword: e.detail.value
        })
        this.setData({
          passwordObj: passwordObj
        });
        this.setPwd()
      }
    }
  },
  setPwd: function () {
    const that = this;
    if (this.data.passwordObj.newPassword !== this.data.passwordObj.rePassword) {
      wx.showToast({
        title: "二次确认密码不一致",
        icon:'none',
        duration: 4000
      });
      const passwordObj = Object.assign({}, this.data.passwordObj, {
        newPassword: '',
        rePassword: '',
      })
      this.setData({
        pwdVal: '',
        payFocus: true,
        type: this.data.isSetPassword === 1 ? 2 : 1,
        passwordObj: passwordObj
      });
      return;
    }
    const utilApi = {
      api: this.data.isSetPassword === 1 ? api.walletUpdate : api.walletAdd,
    }
    if (this.data.isSetPassword === 1) {
      utilApi.data = this.data.passwordObj
    } else {
      utilApi.data = {
        newPassword: this.data.passwordObj.newPassword,
        rePassword: this.data.passwordObj.rePassword,
      }
    }
    util.request(utilApi.api, utilApi.data, 'POST').then(function (res) {
      if (res.success) {
        wx.showToast({
          title: that.data.isSetPassword === 1 ? '修改成功' : '设置成功',
          icon: "success",
          duration: 1000
        });
        setTimeout(() => {
          wx.navigateBack({
            delta: 1
          })
        }, 1000)
      } else {
        const passwordObj = Object.assign({}, that.data.passwordObj, {
          newPassword: '',
          oldPassword:'',
          rePassword:''
        })
        that.setData({
          type: 1,
          pwdVal: '',
          payFocus: true,
          passwordObj: passwordObj
        });
        wx.showToast({
          title: res.msg,
          icon:'none',
          duration: 4000
        });
      }
    })
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

  }
})