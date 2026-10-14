package com.yr.seckilldemo.service;

import com.yr.seckilldemo.pojo.Order;
import com.baomidou.mybatisplus.spring.service.IService;
import com.yr.seckilldemo.pojo.User;
import com.yr.seckilldemo.pojo.seckillMessage;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhoubin
 * @since 2026-09-21
 */
public interface IOrderService extends IService<Order> {



    Long getOrderid(Long goodsOd, User user);
}
