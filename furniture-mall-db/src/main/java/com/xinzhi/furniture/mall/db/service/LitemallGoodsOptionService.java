package com.xinzhi.furniture.mall.db.service;

import com.google.common.collect.Lists;
import com.xinzhi.furniture.mall.db.dao.LitemallGoodsOptionMapper;
import com.xinzhi.furniture.mall.db.domain.LitemallGoodsOption;
import com.xinzhi.furniture.mall.db.domain.LitemallGoodsOptionExample;
import com.xinzhi.furniture.mall.db.service.resp.goods.GoodsOption;
import com.xinzhi.furniture.mall.db.service.resp.goods.GoodsOptionResp;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LitemallGoodsOptionService {

    @Autowired
    private LitemallGoodsOptionMapper litemallGoodsOptionMapper;

    public GoodsOption getLitemallGoodsOptionList(Integer goodsId) {
        LitemallGoodsOptionExample example = new LitemallGoodsOptionExample();
        example.or().andGoodsIdEqualTo(goodsId);
        example.setOrderByClause(LitemallGoodsOption.Column.id.desc());
        List<LitemallGoodsOption> litemallGoodsOptionList = litemallGoodsOptionMapper.selectByExample(example);
        List<GoodsOptionResp> p01List = Lists.newArrayList();
        List<GoodsOptionResp> p02List = Lists.newArrayList();
        List<GoodsOptionResp> p03List = Lists.newArrayList();
        for(LitemallGoodsOption option : litemallGoodsOptionList) {
            GoodsOptionResp resp = new GoodsOptionResp();
            BeanUtils.copyProperties(option, resp);
            if("P01".equalsIgnoreCase(option.getType())) {
                p01List.add(resp);
            } else if("P02".equalsIgnoreCase(option.getType())) {
                p02List.add(resp);
            } else if("P03".equalsIgnoreCase(option.getType())) {
                p03List.add(resp);
            }
        }
        GoodsOption goodsOption = new GoodsOption();
        goodsOption.setP01List(p01List);
        goodsOption.setP02List(p02List);
        goodsOption.setP03List(p03List);
        return goodsOption;
    }

}
