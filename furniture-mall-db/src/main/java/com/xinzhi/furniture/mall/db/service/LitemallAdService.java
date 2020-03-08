package com.xinzhi.furniture.mall.db.service;

import com.google.common.collect.Lists;
import com.xinzhi.furniture.mall.db.dao.LitemallAdMapper;
import com.xinzhi.furniture.mall.db.domain.LitemallAd;
import com.xinzhi.furniture.mall.db.domain.LitemallAdExample;
import com.xinzhi.furniture.mall.db.result.ErrorCodeEnum;
import com.xinzhi.furniture.mall.db.result.PageData;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.req.ad.AdAddReq;
import com.xinzhi.furniture.mall.db.service.req.ad.AdListReq;
import com.xinzhi.furniture.mall.db.service.req.ad.AdUpdateReq;
import com.xinzhi.furniture.mall.db.service.resp.ad.AdListResp;
import com.xinzhi.furniture.mall.db.service.resp.ad.AdResp;
import com.xinzhi.furniture.mall.db.util.Constant;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallAdService {
    @Resource
    private LitemallAdMapper adMapper;

    public List<LitemallAd> queryIndex() {
        LitemallAdExample example = new LitemallAdExample();
        example.or().andDeletedEqualTo(false).andEnabledEqualTo(true);
        return adMapper.selectByExample(example);
    }

    public Result<PageData<AdListResp>> querySelective(AdListReq req) {
        LitemallAdExample example = new LitemallAdExample();
        LitemallAdExample.Criteria criteria = example.createCriteria();

        String name = req.getName();
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        criteria.andDeletedEqualTo(false);
        long count = adMapper.countByExample(example);
        example.setOrderByClause(LitemallAd.Column.addTime.desc() + " " + Constant.LIMIT + " " + (req.getPageIndex() - 1) * req.getPageSize() + "," + req.getPageSize());
        List<AdListResp> adRespList = Lists.newArrayList();
        List<LitemallAd> litemallAdList = adMapper.selectByExampleSelective(example);
        for(LitemallAd ad : litemallAdList) {
            AdListResp resp = new AdListResp();
            BeanUtils.copyProperties(ad, resp);
            adRespList.add(resp);
        }
        PageData<AdListResp> pageData = new PageData<>();
        pageData.setList(adRespList);
        pageData.setTotal(count);
        pageData.setPageSize(req.getPageSize());
        pageData.setPageIndex(req.getPageIndex());
        return Result.ok().setResult(pageData);
    }

    public Result<AdResp> updateById(AdUpdateReq req) {
        LitemallAd ad = new LitemallAd();
        BeanUtils.copyProperties(req, ad);

        ad.setUpdateTime(LocalDateTime.now());
        adMapper.updateByPrimaryKeySelective(ad);

        AdResp resp = new AdResp();
        BeanUtils.copyProperties(ad, resp);
        return Result.ok().setResult(resp);
    }

    public void deleteById(Integer id) {
        adMapper.logicalDeleteByPrimaryKey(id);
    }

    public Result<AdResp> add(AdAddReq req) {
        LitemallAd ad = new LitemallAd();
        BeanUtils.copyProperties(req, ad);
        ad.setAddTime(LocalDateTime.now());
        ad.setUpdateTime(LocalDateTime.now());
        adMapper.insertSelective(ad);

        AdResp resp = new AdResp();
        BeanUtils.copyProperties(ad, resp);
        return Result.ok().setResult(resp);
    }

    public Result<AdResp> findById(String source, Integer id) {
        LitemallAd litemallAd = adMapper.selectByPrimaryKey(id);
        if(litemallAd == null || litemallAd.getDeleted() == LitemallAd.Deleted.IS_DELETED.value()) {
            return Result.fail(ErrorCodeEnum.INPUT_ERROR);
        }
        if("api".equalsIgnoreCase(source) && !litemallAd.getEnabled()) {
            return Result.fail(ErrorCodeEnum.INPUT_ERROR);
        }
        AdResp resp = new AdResp();
        BeanUtils.copyProperties(litemallAd, resp);
        return Result.ok().setResult(resp);
    }

}
