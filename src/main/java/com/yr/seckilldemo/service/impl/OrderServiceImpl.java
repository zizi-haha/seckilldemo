package com.yr.seckilldemo.service.impl;

import com.yr.seckilldemo.pojo.Order;
import com.yr.seckilldemo.mapper.OrderMapper;
import com.yr.seckilldemo.pojo.User;
import com.yr.seckilldemo.pojo.seckillMessage;
import com.yr.seckilldemo.service.IOrderService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhoubin
 * @since 2026-09-21
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {


    @Autowired
    private OrderMapper orderMapper;
    @Override
    public Long getOrderid(Long goodsId,User user) {

        Long l=orderMapper.getOrderId(user.getId(), goodsId);
        return l;
    }
}
