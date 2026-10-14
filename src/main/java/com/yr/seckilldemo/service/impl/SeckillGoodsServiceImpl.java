package com.yr.seckilldemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.yr.seckilldemo.exception.GlobalException;
import com.yr.seckilldemo.mapper.OrderMapper;
import com.yr.seckilldemo.mapper.SeckillGoodsMapper;
import com.yr.seckilldemo.mapper.SeckillOrderMapper;
import com.yr.seckilldemo.pojo.Order;
import com.yr.seckilldemo.pojo.SeckillGoods;
import com.yr.seckilldemo.pojo.User;
import com.yr.seckilldemo.service.ISeckillGoodsService;
import com.yr.seckilldemo.utils.MD5Utils;
import com.yr.seckilldemo.utils.UUidUtils;
import com.yr.seckilldemo.vo.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods>
        implements ISeckillGoodsService {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long doSeckill(Long userId, Long goodsId) {
        int affectedRows = seckillGoodsMapper.doSeckill(goodsId);
        if (affectedRows == 0) {
            throw new GlobalException(ResultEnum.SECKILL_FAIL);
        }

        SeckillGoods seckillGoods = seckillGoodsMapper.selectOne(
                new LambdaQueryWrapper<SeckillGoods>()
                        .eq(SeckillGoods::getGoodsId, goodsId)
        );
        if (seckillGoods == null) {
            throw new GlobalException(ResultEnum.SECKILL_FAIL);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setGoodsId(goodsId);
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel((byte) 1);
        order.setStatus((byte) 0);
        order.setCreateDate(new Date());
        orderMapper.insertOrder(order);

        if (order.getId() == null) {
            throw new GlobalException(ResultEnum.SECKILL_FAIL);
        }

        seckillOrderMapper.insertSeOrder(userId, goodsId, order.getId());
        return order.getId();
    }

    @Override
    public boolean getOne(User user, Long goodsId) {
        return seckillOrderMapper.getOne(user.getId(), goodsId) > 0;
    }

    @Override
    public int getSeckillCount(Long goodsId) {
        return seckillGoodsMapper.getSeckillCount(goodsId);
    }

    @Override
    public String createSeckillPath(User user, Long goodsId) {
        String path = MD5Utils.md5(UUidUtils.uuid() + "123456");
        stringRedisTemplate.opsForValue().set(
                buildPathKey(user.getId(), goodsId),
                path,
                5,
                TimeUnit.MINUTES
        );

        return path;
    }

    @Override
    /**
     * 检查请求路径是否与Redis中存储的路径匹配
     * @param path 请求的路径
     * @param user 发起请求的用户对象
     * @param goodsId 商品ID
     * @return 如果路径匹配且不为null，返回true；否则返回false
     */
    public Boolean checkPath(String path,User user, Long goodsId) {
        if (user == null) {
            return false;
        }

        // 从Redis中获取存储的路径，键为"createPath:用户ID:商品ID"
        String redisPath = stringRedisTemplate.opsForValue()
                .get(buildPathKey(user.getId(), goodsId));

        // 比较请求路径和Redis中存储的路径是否相同，并且确保路径不为null
        return path != null && path.equals(redisPath);
    }

    /**
     * 写入和校验秒杀凭证必须使用同一个 key，否则校验永远失败。
     */
    private String buildPathKey(Long userId, Long goodsId) {
        return "createPath:" + userId + ":" + goodsId;
    }
}
