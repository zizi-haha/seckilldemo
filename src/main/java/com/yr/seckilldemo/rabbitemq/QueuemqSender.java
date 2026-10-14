package com.yr.seckilldemo.rabbitemq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QueuemqSender {


    @Autowired
    private RabbitTemplate rabbitTemplate;
//    public void send(Object message){
//        log.info("发送消息{}",message);
//        rabbitTemplate.convertAndSend("finout_exchange","",message);
//    }
//
//    public void senddericte1(Object message){
//        log.info("directkey1发送消息{}",message);
//        rabbitTemplate.convertAndSend("direct1exchange","directkey1",message);
//    }
//    public void senddericte2(Object message){
//        log.info("directkey2发送消息{}",message);
//        rabbitTemplate.convertAndSend("direct2exchange","directkey2",message);
//    }


    public void sendSeckillmessage(String message){
        log.info("发送消息{}",message);
        rabbitTemplate.convertAndSend("seckillExchange","seckill.message",message);
    }
}
