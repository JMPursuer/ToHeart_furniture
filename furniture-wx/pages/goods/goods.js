var app = getApp();
var WxParse = require('../../lib/wxParse/wxParse.js');
var util = require('../../utils/util.js');
var api = require('../../config/api.js');
var user = require('../../utils/user.js');

Page({
  data: {
    canShare: false,
    id: 0,
    goods: {},
    soldout: false,
    openAttr: false,
    productList: [],
    productListChecked:[],
    product: {},
    choose: {
      p01List: {
        code: '',
        status: 1
      },
      p02List: {
        code: '',
        status: 1
      },
      p03List: {
        code: '',
        status: 1
      },
      number: 1,
    },
    option: {
      p01List: [],
      p02List: [],
      p03List: []
    },
    issueList: []
  },

  // 页面分享
  onShareAppMessage: function () {
    let that = this;
    return {
      title: that.data.goods.name,
      desc: that.data.goods.brief,
      path: '/pages/index/index?goodId=' + this.data.id
    }
  },

  shareFriendOrCircle: function () {
    //var that = this;
    if (this.data.openShare === false) {
      this.setData({
        openShare: !this.data.openShare
      });
    } else {
      return false;
    }
  },
  handleSetting: function (e) {
    var that = this;
    if (!e.detail.authSetting['scope.writePhotosAlbum']) {
      wx.showModal({
        title: '警告',
        content: '不授权无法保存',
        showCancel: false
      })
      that.setData({
        canWrite: false
      })
    } else {
      wx.showToast({
        title: '保存成功'
      })
      that.setData({
        canWrite: true
      })
    }
  },
  // 保存分享图
  saveShare: function () {
    let that = this;
    wx.downloadFile({
      url: that.data.shareImage,
      success: function (res) {
        wx.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: function (res) {
            wx.showModal({
              title: '存图成功',
              content: '图片成功保存到相册了，可以分享到朋友圈了',
              showCancel: false,
              confirmText: '好的',
              confirmColor: '#a78845',
              success: function (res) {
                if (res.confirm) {
                  console.log('用户点击确定');
                }
              }
            })
          },
          fail: function (res) {
            console.log('fail')
          }
        })
      },
      fail: function () {
        console.log('fail')
      }
    })
  },
  // 获取商品信息
  getGoodsInfo: function () {
    let that = this;
    util.request(api.GoodsDetail, {
      id: that.data.id
    }).then(function (res) {
      if (res.success) {
        res.data.info.attribute = res.data.attribute
        res.data.info.gallery = res.data.info.gallery
        that.setData({
          goods: res.data.info,
          issueList: res.data.issue,
          canShare: res.data.share,
          productList: res.data.productList,
          productListChecked:res.data.productList.filter(ret=>(ret.isSpot===1)),
          option: Object.assign({}, res.data.option)
        });
        WxParse.wxParse('goodsDetail', 'html', res.data.info.detail, that);
      }
    });
  },
  chooseCheckedOption(event){
    const item = event.currentTarget.dataset.item;
    item.code = item.code.replace(this.data.goods.goodsSn,'');
    const p03List = this.data.option.p03List.filter(res => (item.code.indexOf(res.code) !== -1));
    item.code = item.code.replace(p03List[0].code,'');
    const p01List = this.data.option.p01List.filter(res => (item.code.indexOf(res.code) !== -1));
    const p02List = this.data.option.p02List.filter(res => (item.code.indexOf(res.code) !== -1));
    this.setData({
      choose: Object.assign({}, this.data.choose, {
        p01List: p01List.length > 0 ? p01List[0] : {},
        p02List: p01List.length > 0 ? p02List[0] : {},
        p03List: p01List.length > 0 ? p03List[0] : {},
      }),
    });
    this.getDetail();
    this.setData({
      openAttr: true
    });
  },
  chooseOption(event) {
    const type = Number(event.currentTarget.dataset.type);
    const item = event.currentTarget.dataset.item;
    if (item.status !== 1) {
      return;
    }
    switch (type) {
      case 1:
        this.setData({
          choose: Object.assign({}, this.data.choose, {
            p01List: item
          })
        });
        break;
      case 2:
        this.setData({
          choose: Object.assign({}, this.data.choose, {
            p02List: item
          })
        });
        break;
      case 3:
        this.setData({
          choose: Object.assign({}, this.data.choose, {
            p03List: item
          })
        });
        break;
    }
    this.getDetail();
  },
  getBuy(){
    util.request(api.CartFastAdd, {
      goodsId: this.data.goods.id,
      number: this.data.choose.number,
      productId: this.data.product.id
    },'POST').then((res)=> {
      if (res.success) {
        wx.showToast({
          title: '购买成功',
          duration:1000
        });
        this.setData({
          openAttr: false
        });
      wx.navigateTo({
        url: `/pages/checkout/checkout?cartId=${res.data}&buyType=1`
      })
      }
    }).catch(e=>{
      
      console.log(e)
      wx.showToast({
        title: '添加失败',
        icon:'none',
        duration:1000
      });
    });
  },
  getCat(){
    util.request(api.CartAdd, {
      goodsId: this.data.goods.id,
      number: this.data.choose.number,
      productId: this.data.product.id
    },'POST').then((res)=> {
      if (res.success) {
        wx.showToast({
          title: '添加成功',
          duration:1000
        });
        this.setData({
          openAttr: false,
          cartGoodsCount:res.data
        });
      }
    }).catch(e=>{
      
      console.log(e)
      wx.showToast({
        title: '添加失败',
        icon:'none',
        duration:1000
      });
    });
  },
  getDetail() {
    const code = this.data.goods.goodsSn + this.data.choose.p01List.code + this.data.choose.p02List.code + this.data.choose.p03List.code
    const desc = `${this.data.choose.p01List.desc}、${this.data.choose.p02List.desc}、${this.data.choose.p03List.desc}`
    const dataList = this.data.productList.filter(res => (res.code === code))
    if(dataList.length === 0){
      this.setData({
        product: Object.assign({}, {
          price: 0.00,
          desc: '',
          url:''
        })
      });
      wx.showToast({
        title: '此商品无库存',
        icon:'none',
        duration:1000
      });
    }
    if (dataList.length === 1) {
      this.setData({
        product: Object.assign({}, dataList[0], {
          price: dataList[0].price.toFixed(2),
          desc: desc,
          isSpot:dataList[0].isSpot
        })
      });
    }
  },
  onLoad: function (options) {
    // 页面初始化 options为页面跳转所带来的参数
    if (options.id) {
      this.setData({
        id: parseInt(options.id)
      });
      this.getGoodsInfo();
    }
    let that = this;
    wx.getSetting({
      success: function (res) {
        //不存在相册授权
        if (!res.authSetting['scope.writePhotosAlbum']) {
          wx.authorize({
            scope: 'scope.writePhotosAlbum',
            success: function () {
              that.setData({
                canWrite: true
              })
            },
            fail: function (err) {
              that.setData({
                canWrite: false
              })
            }
          })
        } else {
          that.setData({
            canWrite: true
          });
        }
      }
    })
  },
  onShow: function () {
    // 页面显示
    var that = this;
    util.request(api.CartGoodsCount).then(function (res) {
      if (res.success) {
        that.setData({
          cartGoodsCount: res.data
        });
      }
    });
  },

  //添加到购物车
  openAttrFun: function () {
    if (this.data.choose.p01List.code === '' && this.data.option.p01List.length > 0) {
      const p01List = this.data.option.p01List.filter(res => (res.status === 1))
      this.setData({
        choose: Object.assign({}, this.data.choose, {
          p01List: p01List.length > 0 ? p01List[0] : {}
        })
      });
    }
    if (this.data.choose.p02List.code === '' && this.data.option.p02List.length > 0) {
      const p02List = this.data.option.p02List.filter(res => res.status === 1)
      this.setData({
        choose: Object.assign({}, this.data.choose, {
          p02List: p02List.length > 0 ? p02List[0] : {}
        })
      });
    }
    if (this.data.choose.p03List.code === '' && this.data.option.p03List.length > 0) {
      const p03List = this.data.option.p03List.filter(res => res.status === 1)
      this.setData({
        choose: Object.assign({}, this.data.choose, {
          p03List: p03List.length > 0 ? p03List[0] : {}
        })
      });
    }
    this.getDetail();
    this.setData({
      openAttr: true
    });
  },

  cutNumber: function () {
    const numberChange = Object.assign({}, this.data.choose, {
      number: this.data.choose.number - 1 > 1 ? this.data.choose.number - 1 : 1
    })
    this.setData({
      choose: numberChange
    });
  },
  addNumber: function () {
    const numberChange = Object.assign({}, this.data.choose, {
      number: this.data.choose.number + 1
    })
    this.setData({
      choose: numberChange
    });
  },
  onHide: function () {
    // 页面隐藏

  },
  onUnload: function () {
    // 页面关闭

  },
  switchAttrPop: function () {
    if (this.data.openAttr == false) {
      this.setData({
        openAttr: !this.data.openAttr
      });
    }
  },
  closeAttr: function () {
    this.setData({
      openAttr: false,
    });
  },
  closeShare: function () {
    this.setData({
      openShare: false,
    });
  },
  openCartPage: function () {
    wx.switchTab({
      url: '/pages/cart/cart'
    });
  },
  onReady: function () {
    // 页面渲染完成

  }

})