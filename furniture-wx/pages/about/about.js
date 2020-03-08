const util = require('../../utils/util.js');
const api = require('../../config/api.js');

//获取应用实例
const app = getApp();

Page({

  /**
   * 页面的初始数据
   */
  data: {
    address: '心至地址',
    phone: '021-xxxx-xxxx'
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    this.getAbout()
  },
  getAbout: function () {
    let that = this;
    util.request(api.AboutUrl).then(function (res) {
      if (res.success) {
        that.setData({
          address: res.data.address,
          phone: res.data.phone
        });
      }
    });
  },
  showLocation: function (e) {
    var that = this
    wx.openLocation({
      latitude: parseFloat(that.data.latitude),
      longitude: parseFloat(that.data.longitude),
      name: that.data.name,
      address: that.data.address,
    })
  },
  callPhone: function (e) {
    var that = this
    wx.makePhoneCall({
      phoneNumber: that.data.phone,
    })
  }
})