package com.yr.seckilldemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yr.seckilldemo.pojo.Goods;
import com.yr.seckilldemo.vo.GoodsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GoodsMapper extends BaseMapper<Goods> {

    List<GoodsVO> selectGoodes();

    GoodsVO selectGoodsById(@Param("goodsId") Long goodsId);
}
