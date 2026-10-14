package com.yr.seckilldemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key 用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value 用 RedisTemplate 默认的 JDK 序列化（Spring Data Redis 4.x 推荐）
        // 如果你想要 JSON，可以用 Jackson2JsonRedisSerializer，但它在新版里也被标记了，
        // 最简单的方式：直接用默认序列化器

        template.afterPropertiesSet();   // 初始化
        return template;                  // 必须 return！
    }
}