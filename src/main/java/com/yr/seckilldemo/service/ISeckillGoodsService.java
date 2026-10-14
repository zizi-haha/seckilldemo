package com.yr.seckilldemo.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.yr.seckilldemo.pojo.SeckillGoods;
import com.yr.seckilldemo.pojo.User;

public interface ISeckillGoodsService extends IService<SeckillGoods> {

    Long doSeckill(Long userId, Long goodsId);

    boolean getOne(User user, Long goodsId);

    int getSeckillCount(Long goodsId);

    String createSeckillPath(User user, Long goodsId);

    Boolean checkPath(String path,User user, Long goodsId);
}
