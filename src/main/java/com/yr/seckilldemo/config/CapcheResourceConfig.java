package com.yr.seckilldemo.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.spring.plugins.RedisResourceStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
@Configuration

public class CapcheResourceConfig {





        @Bean
        public ResourceStore captchaResourceStore(StringRedisTemplate redisTemplate) {
            RedisResourceStore resourceStore = new RedisResourceStore(redisTemplate);

            resourceStore.setResources(
                    CaptchaTypeConstant.SLIDER,
                    null,
                    Collections.singletonList(
                            new Resource(
                                    "classpath",
                                    "META-INF/cut-image/resource/1.jpg"
                            )
                    )
            );

            return resourceStore;
        }
    }

