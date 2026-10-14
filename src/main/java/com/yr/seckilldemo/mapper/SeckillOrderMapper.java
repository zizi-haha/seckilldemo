package com.yr.seckilldemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yr.seckilldemo.pojo.SeckillOrder;
import org.apache.ibatis.annotations.Param;

public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {

    int getOne(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    void insertSeOrder(
            @Param("userId") Long userId,
            @Param("goodsId") Long goodsId,
            @Param("orderId") Long orderId);
}
