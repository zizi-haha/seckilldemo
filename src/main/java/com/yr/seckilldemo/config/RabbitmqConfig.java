package com.yr.seckilldemo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class RabbitmqConfig {

    private static final String QUEUE = "seckillQueue";
    private static  final String EXCHANGE = "seckillExchange";
    private  static  final String ROUTINGKEY = "seckill.#";

    private static final String DXL_EXCHAGE="seckillDlExchange";
    private static final String DLQ="seckillDlQueue";
    private static final String DXL_ROUTING_KEY="seckill.dead";
    @Bean
    public Queue seckillQueue(){
        return
                QueueBuilder.durable(QUEUE)
                        .deadLetterExchange(DXL_EXCHAGE)
                        .deadLetterRoutingKey(DXL_ROUTING_KEY)
                        .build();
    }

    @Bean
    public TopicExchange seckillExchange(){
        return  new TopicExchange(EXCHANGE,true,false);
    }


    @Bean
    public Binding seckillbinding(){
        return BindingBuilder.bind(seckillQueue()).to(seckillExchange()).with(ROUTINGKEY);
    }

    @Bean
    public Queue seckilDeadQueue(){
        return QueueBuilder.durable(DLQ).build();//?
    }

    @Bean
    public DirectExchange seckillDlxExchange(){
        return new DirectExchange(DXL_EXCHAGE,true,false);//?
    }

    @Bean
    public Binding deadLetterBinding(){
        return
                BindingBuilder.bind(seckilDeadQueue()).to(seckillDlxExchange()).with(DXL_ROUTING_KEY);
    }
}
