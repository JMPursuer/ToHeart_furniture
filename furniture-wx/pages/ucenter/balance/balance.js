// pages/ucenter/balance/balance.js
var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');
Page({

  /**
   * 页面的初始数据
   */
  data: {
    balance: '0.00',
    searchLoading: false,
    isSetPassword:2,
    balanceList: [],
    total: 0,
    pageIndex: 1
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {},
  onReachBottom: function () {
    if (this.data.searchLoading) {
      return;
    }
    const index = this.data.pageIndex+ 1;
    this.setData({
      pageIndex: index
    });
    this.getList();
  },
  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {
    this.setData({
      pageIndex: 1,
      balanceList:[]
    });
    this.getList();
    this.init();
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
  init: function () {
    util.request(api.walletInfo, {}, 'post').then((res) => {
      if (res.success) {
        this.setData({
          balance: util.fmoney(res.data.balance, 2),
          isSetPassword:res.data.isSetPassword
        });
      }
    });
  },
  getList: function () {
    util.request(api.walletFlow, {
      pageIndex: this.data.pageIndex,
      pageSize: 10
    }, 'post').then((res) => {
      if (res.success) {
        this.setData({
          balanceList: this.data.balanceList.concat(res.data.list.map(res => {
            res.createTime = util.formatTime(new Date(res.createTime))
            if (res.flow > 0) {
              res.checked = true
            } else {
              res.checked = false
              res.flow = Math.abs(res.flow)
            }
            res.balance = util.fmoney(res.balance, 2)
            return res;
          })),
          total: res.data.total,
          searchLoading: (res.data.list.length !==10)
        });
      }
    });
  },
})