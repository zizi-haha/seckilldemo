package com.yr.seckilldemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yr.seckilldemo.pojo.SeckillGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    int doSeckill(@Param("goodsId") Long goodsId);

    @Select("select stock_count from t_seckill_goods where goods_id = #{goodsId}")
    int getSeckillCount(@Param("goodsId") Long goodsId);
}
