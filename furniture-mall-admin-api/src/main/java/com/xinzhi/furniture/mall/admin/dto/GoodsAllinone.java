package com.xinzhi.furniture.mall.admin.dto;

import com.xinzhi.furniture.mall.db.domain.LitemallGoods;
import com.xinzhi.furniture.mall.db.domain.LitemallGoodsAttribute;

public class GoodsAllinone {
    LitemallGoods goods;
    LitemallGoodsAttribute[] attributes;

    public LitemallGoods getGoods() {
        return goods;
    }

    public void setGoods(LitemallGoods goods) {
        this.goods = goods;
    }

    public LitemallGoodsAttribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(LitemallGoodsAttribute[] attributes) {
        this.attributes = attributes;
    }

}
