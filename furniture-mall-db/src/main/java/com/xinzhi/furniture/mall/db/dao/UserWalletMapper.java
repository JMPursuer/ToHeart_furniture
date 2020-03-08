package com.xinzhi.furniture.mall.db.dao;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface UserWalletMapper {

    int increaseBalance(@Param("userId") Integer userId, @Param("amount") BigDecimal amount);

}
