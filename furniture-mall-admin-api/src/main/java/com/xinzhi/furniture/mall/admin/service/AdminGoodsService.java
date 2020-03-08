package com.xinzhi.furniture.mall.admin.service;

import com.google.common.collect.Lists;
import com.xinzhi.furniture.mall.core.qcode.QCodeService;
import com.xinzhi.furniture.mall.db.dao.*;
import com.xinzhi.furniture.mall.db.domain.*;
import com.xinzhi.furniture.mall.db.result.ErrorCodeEnum;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.*;
import com.xinzhi.furniture.mall.db.service.resp.goods.GoodsOption;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.xinzhi.furniture.mall.admin.dto.GoodsAllinone;
import com.xinzhi.furniture.mall.admin.vo.CatVo;
import com.xinzhi.furniture.mall.core.util.ResponseUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminGoodsService {
    private final Log logger = LogFactory.getLog(AdminGoodsService.class);

    @Autowired
    private LitemallGoodsService goodsService;
    @Autowired
    private LitemallGoodsSpecificationService specificationService;
    @Autowired
    private LitemallGoodsAttributeService attributeService;
    @Autowired
    private LitemallGoodsProductService productService;
    @Autowired
    private LitemallCategoryService categoryService;
    @Autowired
    private LitemallBrandService brandService;
    @Autowired
    private LitemallCartService cartService;
    @Autowired
    private QCodeService qCodeService;

    @Autowired
    private LitemallGoodsOptionService litemallGoodsOptionService;

    @Autowired
    private LitemallGoodsMapper litemallGoodsMapper;

    @Autowired
    private LitemallGoodsOptionMapper litemallGoodsOptionMapper;

    @Autowired
    private LitemallGoodsProductMapper litemallGoodsProductMapper;

    @Autowired
    private LitemallGoodsOptionTmpMapper litemallGoodsOptionTmpMapper;

    @Autowired
    private GoodsOptMapper goodsOptMapper;

    public Object list(Integer goodsId, String goodsSn, String name,
                       Integer page, Integer limit, String sort, String order) {
        List<LitemallGoods> goodsList = goodsService.querySelective(goodsId, goodsSn, name, page, limit, sort, order);
        return ResponseUtil.okList(goodsList);
    }

    private Object validate(GoodsAllinone goodsAllinone) {
        LitemallGoods goods = goodsAllinone.getGoods();
        String name = goods.getName();
        if (StringUtils.isEmpty(name)) {
            return ResponseUtil.badArgument();
        }
        String goodsSn = goods.getGoodsSn();
        if (StringUtils.isEmpty(goodsSn)) {
            return ResponseUtil.badArgument();
        }
        // 品牌商可以不设置，如果设置则需要验证品牌商存在
//        Integer brandId = goods.getBrandId();
//        if (brandId != null && brandId != 0) {
//            if (brandService.findById(brandId) == null) {
//                return ResponseUtil.badArgumentValue();
//            }
//        }
        // 分类可以不设置，如果设置则需要验证分类存在
        Integer categoryId = goods.getCategoryId();
        if (categoryId != null && categoryId != 0) {
            if (categoryService.findById(categoryId) == null) {
                return ResponseUtil.badArgumentValue();
            }
        }

        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
        for (LitemallGoodsAttribute attribute : attributes) {
            String attr = attribute.getAttribute();
            if (StringUtils.isEmpty(attr)) {
                return ResponseUtil.badArgument();
            }
            String value = attribute.getValue();
            if (StringUtils.isEmpty(value)) {
                return ResponseUtil.badArgument();
            }
        }

//        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
//        for (LitemallGoodsSpecification specification : specifications) {
//            String spec = specification.getSpecification();
//            if (StringUtils.isEmpty(spec)) {
//                return ResponseUtil.badArgument();
//            }
//            String value = specification.getValue();
//            if (StringUtils.isEmpty(value)) {
//                return ResponseUtil.badArgument();
//            }
//        }

//        LitemallGoodsProduct[] products = goodsAllinone.getProducts();
//        for (LitemallGoodsProduct product : products) {
//            Integer number = product.getNumber();
//            if (number == null || number < 0) {
//                return ResponseUtil.badArgument();
//            }
//
//            BigDecimal price = product.getPrice();
//            if (price == null) {
//                return ResponseUtil.badArgument();
//            }
//
//            String[] productSpecifications = product.getSpecifications();
//            if (productSpecifications.length == 0) {
//                return ResponseUtil.badArgument();
//            }
//        }

        return null;
    }

    /**
     * 编辑商品
     *
     * NOTE：
     * 由于商品涉及到四个表，特别是litemall_goods_product表依赖litemall_goods_specification表，
     * 这导致允许所有字段都是可编辑会带来一些问题，因此这里商品编辑功能是受限制：
     * （1）litemall_goods表可以编辑字段；
     * （2）litemall_goods_specification表只能编辑pic_url字段，其他操作不支持；
     * （3）litemall_goods_product表只能编辑price, number和url字段，其他操作不支持；
     * （4）litemall_goods_attribute表支持编辑、添加和删除操作。
     *
     * NOTE2:
     * 前后端这里使用了一个小技巧：
     * 如果前端传来的update_time字段是空，则说明前端已经更新了某个记录，则这个记录会更新；
     * 否则说明这个记录没有编辑过，无需更新该记录。
     *
     * NOTE3:
     * （1）购物车缓存了一些商品信息，因此需要及时更新。
     * 目前这些字段是goods_sn, goods_name, price, pic_url。
     * （2）但是订单里面的商品信息则是不会更新。
     * 如果订单是未支付订单，此时仍然以旧的价格支付。
     */
    @Transactional
    public Object update(GoodsAllinone goodsAllinone) {
        Object error = validate(goodsAllinone);
        if (error != null) {
            return error;
        }

        LitemallGoods goods = goodsAllinone.getGoods();
        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
//        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
//        LitemallGoodsProduct[] products = goodsAllinone.getProducts();

        //将生成的分享图片地址写入数据库
        String url = qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());
        goods.setShareUrl(url);

        // 商品表里面有一个字段retailPrice记录当前商品的最低价
//        BigDecimal retailPrice = new BigDecimal(Integer.MAX_VALUE);
//        for (LitemallGoodsProduct product : products) {
//            BigDecimal productPrice = product.getPrice();
//            if(retailPrice.compareTo(productPrice) == 1){
//                retailPrice = productPrice;
//            }
//        }
//        goods.setRetailPrice(retailPrice);
        
        // 商品基本信息表litemall_goods
        goods.setGoodsSn(null);
        if (goodsService.updateById(goods) == 0) {
            throw new RuntimeException("更新数据失败");
        }

        Integer gid = goods.getId();

        // 商品规格表litemall_goods_specification
//        for (LitemallGoodsSpecification specification : specifications) {
//            // 目前只支持更新规格表的图片字段
//            if(specification.getUpdateTime() == null){
//                specification.setSpecification(null);
//                specification.setValue(null);
//                specificationService.updateById(specification);
//            }
//        }

        // 商品货品表litemall_product
//        for (LitemallGoodsProduct product : products) {
//            product.setCode(null);
//            product.setGoodsId(null);
//            if(product.getUpdateTime() == null) {
//                productService.updateById(product);
//            }
//        }

        // 商品参数表litemall_goods_attribute
        for (LitemallGoodsAttribute attribute : attributes) {
            if (attribute.getId() == null || attribute.getId().equals(0)){
                attribute.setGoodsId(goods.getId());
                attributeService.add(attribute);
            }
            else if(attribute.getDeleted()){
                attributeService.deleteById(attribute.getId());
            }
            else if(attribute.getUpdateTime() == null){
                attribute.setGoodsId(null);
                attributeService.updateById(attribute);
            }
        }

        // 这里需要注意的是购物车litemall_cart有些字段是拷贝商品的一些字段，因此需要及时更新
        // 目前这些字段是goods_sn, goods_name, price, pic_url
//        for (LitemallGoodsProduct product : products) {
//            cartService.updateProduct(product.getId(), goods.getGoodsSn(), goods.getName(), product.getPrice(), product.getUrl());
//        }

        return ResponseUtil.ok();
    }

    @Transactional
    public Object delete(LitemallGoods goods) {
        Integer id = goods.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }

        Integer gid = goods.getId();
        goodsService.deleteById(gid);
//        specificationService.deleteByGid(gid);
//        attributeService.deleteByGid(gid);
//        productService.deleteByGid(gid);
        return ResponseUtil.ok();
    }

//    @Transactional
//    public Object create(GoodsAllinone goodsAllinone) {
//        Object error = validate(goodsAllinone);
//        if (error != null) {
//            return error;
//        }
//
//        LitemallGoods goods = goodsAllinone.getGoods();
//        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
//        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
//        LitemallGoodsProduct[] products = goodsAllinone.getProducts();
//
//        String name = goods.getName();
//        if (goodsService.checkExistByName(name)) {
//            return ResponseUtil.fail(GOODS_NAME_EXIST, "商品名已经存在");
//        }
//
//        // 商品表里面有一个字段retailPrice记录当前商品的最低价
//        BigDecimal retailPrice = new BigDecimal(Integer.MAX_VALUE);
//        for (LitemallGoodsProduct product : products) {
//            BigDecimal productPrice = product.getPrice();
//            if(retailPrice.compareTo(productPrice) == 1){
//                retailPrice = productPrice;
//            }
//        }
//        goods.setRetailPrice(retailPrice);
//
//        // 商品基本信息表litemall_goods
//        goodsService.add(goods);
//
//        //将生成的分享图片地址写入数据库
//        String url = qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());
//        if (!StringUtils.isEmpty(url)) {
//            goods.setShareUrl(url);
//            if (goodsService.updateById(goods) == 0) {
//                throw new RuntimeException("更新数据失败");
//            }
//        }
//
//        // 商品规格表litemall_goods_specification
//        for (LitemallGoodsSpecification specification : specifications) {
//            specification.setGoodsId(goods.getId());
//            specificationService.add(specification);
//        }
//
//        // 商品参数表litemall_goods_attribute
//        for (LitemallGoodsAttribute attribute : attributes) {
//            attribute.setGoodsId(goods.getId());
//            attributeService.add(attribute);
//        }
//
//        // 商品货品表litemall_product
//        for (LitemallGoodsProduct product : products) {
//            product.setGoodsId(goods.getId());
//            productService.add(product);
//        }
//        return ResponseUtil.ok();
//    }

    public Object list2() {
        // http://element-cn.eleme.io/#/zh-CN/component/cascader
        // 管理员设置“所属分类”
        List<LitemallCategory> l1CatList = categoryService.queryL1();
        List<CatVo> categoryList = new ArrayList<>(l1CatList.size());

        for (LitemallCategory l1 : l1CatList) {
            CatVo l1CatVo = new CatVo();
            l1CatVo.setValue(l1.getId());
            l1CatVo.setLabel(l1.getName());

            List<LitemallCategory> l2CatList = categoryService.queryByPid(l1.getId());
            List<CatVo> children = new ArrayList<>(l2CatList.size());
            for (LitemallCategory l2 : l2CatList) {
                CatVo l2CatVo = new CatVo();
                l2CatVo.setValue(l2.getId());
                l2CatVo.setLabel(l2.getName());
                children.add(l2CatVo);
            }
            l1CatVo.setChildren(children);

            categoryList.add(l1CatVo);
        }

        // http://element-cn.eleme.io/#/zh-CN/component/select
        // 管理员设置“所属品牌商”
        List<LitemallBrand> list = brandService.all();
        List<Map<String, Object>> brandList = new ArrayList<>(l1CatList.size());
        for (LitemallBrand brand : list) {
            Map<String, Object> b = new HashMap<>(2);
            b.put("value", brand.getId());
            b.put("label", brand.getName());
            brandList.add(b);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("categoryList", categoryList);
        data.put("brandList", brandList);
        return ResponseUtil.ok(data);
    }

    public Object detail(Integer id) {
        LitemallGoods goods = goodsService.findById(id);
        List<LitemallGoodsProduct> products = productService.queryByGid(id);
        GoodsOption options = litemallGoodsOptionService.getLitemallGoodsOptionList(id);
        List<LitemallGoodsAttribute> attributes = attributeService.queryByGid(id);

        Integer categoryId = goods.getCategoryId();
        LitemallCategory category = categoryService.findById(categoryId);
        Integer[] categoryIds = new Integer[]{};
        if (category != null) {
            Integer parentCategoryId = category.getPid();
            categoryIds = new Integer[]{parentCategoryId, categoryId};
        }

        Map<String, Object> data = new HashMap<>();
        data.put("goods", goods);
        data.put("option", options);
        data.put("products", products);
        data.put("attributes", attributes);
        data.put("categoryIds", categoryIds);

        return ResponseUtil.ok(data);
    }

    public Result<List<LitemallGoodsOptionTmp>> add(List<LitemallGoodsOptionTmp> optionList) {
        LitemallGoodsOptionTmpExample example = LitemallGoodsOptionTmpExample.newAndCreateCriteria().example();
        example.orderBy(LitemallGoodsOptionTmp.Column.id.asc());
        List<LitemallGoodsOptionTmp> optList = litemallGoodsOptionTmpMapper.selectByExample(example);
        if(optList != null && optList.size() > 0) {
            return Result.fail(ErrorCodeEnum.OPTION_TMP_NO_HANDLE_ERROR);
        }
        goodsOptMapper.addOptionTmp(optionList);
        LitemallGoodsOptionTmpExample optExample = LitemallGoodsOptionTmpExample.newAndCreateCriteria().example();
        optExample.orderBy(LitemallGoodsOptionTmp.Column.id.asc());
        List<LitemallGoodsOptionTmp> optionTmpList = litemallGoodsOptionTmpMapper.selectByExample(optExample);
        return Result.ok().setResult(optionTmpList);
    }

    public Result insertOrUpdate() {
        LitemallGoodsOptionTmpExample example = LitemallGoodsOptionTmpExample.newAndCreateCriteria().example();
        example.orderBy(LitemallGoodsOptionTmp.Column.id.asc());
        List<LitemallGoodsOptionTmp> optionTmpList = litemallGoodsOptionTmpMapper.selectByExample(example);

        List<LitemallGoodsOption> ppList = Lists.newArrayList();
        List<LitemallGoodsOption> p01List = Lists.newArrayList();
        List<LitemallGoodsOption> p02List = Lists.newArrayList();
        List<LitemallGoodsOption> p03List = Lists.newArrayList();

        for(LitemallGoodsOptionTmp tmpOption : optionTmpList) {
            LitemallGoodsOption option = new LitemallGoodsOption();
            BeanUtils.copyProperties(tmpOption, option);
            option.setAddTime(Instant.now().toEpochMilli());
            option.setUpdateTime(Instant.now().toEpochMilli());
            option.setId(null);
            String type = tmpOption.getType();
            if("PP".equalsIgnoreCase(type)) {
                ppList.add(option);
            } else if("P01".equalsIgnoreCase(type)) {
                p01List.add(option);
            } else if("P02".equalsIgnoreCase(type)) {
                p02List.add(option);
            } else if("P03".equalsIgnoreCase(type)) {
                p03List.add(option);
            }
        }

        if(ppList.size() == 0 || p01List.size() == 0 || p02List.size() == 0 || p03List.size() == 0) {
            return Result.fail(ErrorCodeEnum.OPTION_INCOMPLETE_ERROR);
        }

        for(LitemallGoodsOption pp : ppList) {
            // 添加到 goods
            LitemallGoods goods = litemallGoodsMapper.selectOneByExampleSelective(LitemallGoodsExample.newAndCreateCriteria().andGoodsSnEqualTo(pp.getCode()).example());
            if(goods != null) {
                goods.setIsOnSale(pp.getStatus() == 0 ? false : true);
                goods.setName(pp.getDesc());
                litemallGoodsMapper.updateByPrimaryKeySelective(goods);
            } else {
                goods = new LitemallGoods();
                goods.setGoodsSn(pp.getCode());
                goods.setName(pp.getDesc());
                goods.setIsOnSale(false);
                litemallGoodsMapper.insertSelective(goods);
            }

            List<LitemallGoodsOption> optionList = Lists.newArrayList();
            // 根据 pp 筛选 p01 添加到 option
            List<LitemallGoodsOption> p01TmpList = Lists.newArrayList();
            for(LitemallGoodsOption p01 : p01List) {
                if(p01.getMaterial().equalsIgnoreCase(pp.getMaterial())) {
                    p01.setGoodsId(goods.getId());
                    p01TmpList.add(p01);
                }
            }
            optionList.addAll(p01TmpList);

            // 添加所有的 p02 到 option
            for(LitemallGoodsOption p02 : p02List) {
                p02.setGoodsId(goods.getId());
            }
            optionList.addAll(p02List);

            // 添加所有的 p03 到 option
            for(LitemallGoodsOption p03 : p03List) {
                p03.setGoodsId(goods.getId());
            }
            optionList.addAll(p03List);
            if(optionList != null && optionList.size() > 0) {
                goodsOptMapper.addOption(optionList);
            }

            List<String> codeList = Lists.newArrayList();
            List<LitemallGoodsProduct> productList = Lists.newArrayList();
            for(LitemallGoodsOption p01 : p01TmpList) {
                for(LitemallGoodsOption p02 : p02List) {
                    for(LitemallGoodsOption p03 : p03List) {
                        String code = pp.getCode() + p01.getCode() + p02.getCode() + p03.getCode();
                        if(pp.getStatus() == 0 || p01.getStatus() == 0 || p02.getStatus() == 0 || p03.getStatus() == 0) {
                            // 存在无效code，就删除 product
                            codeList.add(code);
                            continue;
                        }
                        LitemallGoodsProduct product = new LitemallGoodsProduct();
                        product.setGoodsId(goods.getId());
                        product.setCode(code);
                        product.setAddTime(LocalDateTime.now());
                        product.setUpdateTime(LocalDateTime.now());
                        product.setPrice(pp.getPrice().add(p01.getPrice()).add(p02.getPrice()).add(p03.getPrice()));
                        product.setUrl(p01.getImage());
                        product.setTitle(pp.getDesc() + " " + p01.getDesc() + " " + p02.getDesc() + " " + p03.getDesc());
                        productList.add(product);
                    }
                }
            }
            if(productList != null && productList.size() > 0) {
                goodsOptMapper.addProduct(productList);
            }
            if(codeList != null && codeList.size() > 0) {
                goodsOptMapper.deleteProduct(codeList);
            }
        }
        // 删除临时数据
        goodsOptMapper.deleteOptionTmp();
        return Result.ok();
    }

    public Result deleteOption() {
        goodsOptMapper.deleteOptionTmp();
        return Result.ok();
    }

    public Result<List<LitemallGoodsOptionTmp>> listOption() {
        LitemallGoodsOptionTmpExample example = LitemallGoodsOptionTmpExample.newAndCreateCriteria().example();
        example.orderBy(LitemallGoodsOptionTmp.Column.id.asc());
        List<LitemallGoodsOptionTmp> optionTmpList = litemallGoodsOptionTmpMapper.selectByExample(example);
        return Result.ok().setResult(optionTmpList);
    }
}
