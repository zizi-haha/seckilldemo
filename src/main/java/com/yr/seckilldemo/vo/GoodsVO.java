package com.yr.seckilldemo.vo;


import com.yr.seckilldemo.pojo.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class GoodsVO extends Goods {
    private Integer stockCount;
    private Double seckillPrice;
    private String startTime;
    private String endTime;
}

