package com.yr.seckilldemo.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.yr.seckilldemo.mapper.GoodsMapper;
import com.yr.seckilldemo.pojo.Goods;
import com.yr.seckilldemo.service.IGoodsService;
import com.yr.seckilldemo.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public List<GoodsVO> findGoods() {
        return goodsMapper.selectGoodes();
    }

    @Override
    public GoodsVO findGoodsByid(Long id) {
        return goodsMapper.selectGoodsById(id);
    }
}
