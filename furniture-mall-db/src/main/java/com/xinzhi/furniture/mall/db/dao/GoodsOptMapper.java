package com.xinzhi.furniture.mall.db.dao;

import com.xinzhi.furniture.mall.db.domain.LitemallGoodsOption;
import com.xinzhi.furniture.mall.db.domain.LitemallGoodsOptionTmp;
import com.xinzhi.furniture.mall.db.domain.LitemallGoodsProduct;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GoodsOptMapper {

    long addOptionTmp(@Param("optionList") List<LitemallGoodsOptionTmp> optionList);

    long deleteOptionTmp();

    long addOption(@Param("optionList") List<LitemallGoodsOption> optionList);

    long addProduct(@Param("productList") List<LitemallGoodsProduct> productList);

    long deleteProduct(@Param("codeList") List<String> codeList);

}