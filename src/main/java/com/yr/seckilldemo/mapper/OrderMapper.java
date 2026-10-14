package com.yr.seckilldemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yr.seckilldemo.pojo.Order;
import com.yr.seckilldemo.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper extends BaseMapper<Order> {

    void insertOrder(Order order);

    Long getOrderId(@Param("userId") Long  userid, @Param("goodsId")Long goodsId);
//TODO p55 p56
}
