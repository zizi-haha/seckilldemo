package com.yr.seckilldemo.controller;


import com.yr.seckilldemo.pojo.User;
import com.yr.seckilldemo.pojo.seckillMessage;
import com.yr.seckilldemo.rabbitemq.QueuemqSender;
import com.yr.seckilldemo.service.IGoodsService;
import com.yr.seckilldemo.service.ISeckillGoodsService;
import com.yr.seckilldemo.vo.GoodsVO;
import com.yr.seckilldemo.vo.Result;
import com.yr.seckilldemo.vo.ResultEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;

import java.util.List;


@Controller
@RequestMapping("/seckill")
public class SeckillGoodsController implements InitializingBean {

    //lua脚本
    private static final DefaultRedisScript<Long> SECKILL_STOCK_SCRIPT =
            new DefaultRedisScript<>(
                    "local stock=redis.call('get',KEYS[1])" +

                            "if stock==false then return -1 end " +

                            "if tonumber(stock)<=0 then return -1 end " +

                            "return redis.call('decr',KEYS[1])",
                    Long.class);


    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ISeckillGoodsService seckillGoodsService;


    @Autowired
    private QueuemqSender queuemqSender;


//windows qps=190.8

    @PostMapping("/doseckill")
    @ResponseBody
    public Result doSeckill(Model model, User user, @RequestParam("goodsid") Long goodsId, String path) {
        ObjectMapper objectMapper = new ObjectMapper();

        if (user == null) {
            return Result.error(ResultEnum.USER_ERROR);
        }
        // path 校验失败就直接结束，不要往下走
        if (seckillGoodsService.checkPath(path, user, goodsId) == false) {
            return Result.error(ResultEnum.ERROR);
        }

        //判断是否重复抢购
        String existingOrderId = stringRedisTemplate.opsForValue().get(
                "order:" + user.getId() + ":" + goodsId
        );
        if (existingOrderId != null) {
            return Result.error(ResultEnum.ERROR);
        }

        //库存预减：判断和扣减在 Redis 内一次原子完成
        Long remain = stringRedisTemplate.execute(
                SECKILL_STOCK_SCRIPT,
                Collections.singletonList("seckillGoods:" + goodsId));
        if (remain == null || remain < 0) {
            return Result.error(ResultEnum.EMPLY_ERROR);
        }

        //进行秒杀
        seckillMessage seckillMessage = new seckillMessage();
        seckillMessage.setUserId(user.getId());
        seckillMessage.setGoodsId(goodsId);
        queuemqSender.sendSeckillmessage(objectMapper.writeValueAsString(seckillMessage));

        return Result.success(0);//0表示正在派对中
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<GoodsVO> list = goodsService.findGoods();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (GoodsVO goodsVO : list) {
            stringRedisTemplate.opsForValue().set(
                    "seckillGoods:" + goodsVO.getId(),
                    String.valueOf(goodsVO.getStockCount())
            );

        }
    }


    @GetMapping("/path")
    @ResponseBody
    public Result createSeckillPath(User user, Long goodsId) {
        if (user == null) {

            return Result.error(ResultEnum.USER_ERROR);
        }
        String path = seckillGoodsService.createSeckillPath(user, goodsId);
        return Result.success(path);

    }
}
