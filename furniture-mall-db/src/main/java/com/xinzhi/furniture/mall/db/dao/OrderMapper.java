package com.xinzhi.furniture.mall.db.dao;

import org.apache.ibatis.annotations.Param;
import com.xinzhi.furniture.mall.db.domain.LitemallOrder;

import java.time.LocalDateTime;

public interface OrderMapper {
    int updateWithOptimisticLocker(@Param("lastUpdateTime") LocalDateTime lastUpdateTime, @Param("order") LitemallOrder order);
}