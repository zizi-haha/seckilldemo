package com.yr.seckilldemo.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.yr.seckilldemo.pojo.Goods;
import com.yr.seckilldemo.vo.GoodsVO;

import java.util.List;

public interface IGoodsService extends IService<Goods> {

    List<GoodsVO> findGoods();

    GoodsVO findGoodsByid(Long id);
}
