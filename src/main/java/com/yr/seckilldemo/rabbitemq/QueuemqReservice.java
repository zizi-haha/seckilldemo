package com.yr.seckilldemo.rabbitemq;


import com.yr.seckilldemo.config.RabbitmqConfig;
import com.yr.seckilldemo.exception.GlobalException;
import com.yr.seckilldemo.pojo.seckillMessage;
import com.yr.seckilldemo.service.ISeckillGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class QueuemqReservice {
    @Autowired
    private ISeckillGoodsService iSeckillGoodsServicer;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "seckillQueue")
    public void receiveSeckillMessage(String message) {
        log.info("接收消息{}", message);

        seckillMessage seckillMessage = new seckillMessage();
        try {
            seckillMessage = objectMapper.readValue(message, seckillMessage.class);
        } catch (Exception e) {
            log.error("消息格式错误 已丢弃{}", message, e);
        }

        try {
            Long orderId = iSeckillGoodsServicer.doSeckill(seckillMessage.getUserId(), seckillMessage.getGoodsId());
            stringRedisTemplate.opsForValue().set("order:" + seckillMessage.getGoodsId() + ":" + seckillMessage.getGoodsId(), String.valueOf(orderId));


        } catch (DuplicateKeyException e) {//违反数据库唯一约束
            //重复购买
            log.error("重复购买 忽略用户：{}", seckillMessage.getUserId());

        } catch (GlobalException e) {
            //库存不足 活动已结束
            log.warn("用户={} 商品= {} reason={}", seckillMessage.getUserId(), seckillMessage.getGoodsId(), e.getResultEnum().getMsg());

        }
        //其他异常会向上抛出 然后根据default -requeue -rejected =false进出死信队列
    }
}

