package com.yr.seckilldemo.controller;

import com.yr.seckilldemo.pojo.User;
import com.yr.seckilldemo.pojo.seckillMessage;
import com.yr.seckilldemo.service.IOrderService;
import com.yr.seckilldemo.vo.Result;
import com.yr.seckilldemo.vo.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhoubin
 * @since 2026-09-21
 */
@Controller
@RequestMapping("/seckill-order")
public class SeckillOrderController {
    //判断redis里面有没有对应的订单号 从而判断是否秒杀成功
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @GetMapping("/result")
    @ResponseBody
    public Result check(Long goodsId, User user) {
        if(user==null){
            return Result.error(ResultEnum.USER_ERROR);
        }

        Long orderId = orderService.getOrderid(goodsId, user);

        if (orderId != null) {
            return Result.success(orderId);
        }

        String stockText = stringRedisTemplate.opsForValue()
                .get("seckillGoods:" + goodsId);


        if (stockText == null) {
            return Result.error(ResultEnum.ERROR);
        }

        long stock = Long.parseLong(stockText);
        return Result.success(stock > 0 ? 0 : -1);
    }
}
