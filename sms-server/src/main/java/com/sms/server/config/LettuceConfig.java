package com.sms.server.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableCaching
public class LettuceConfig extends CachingConfigurerSupport {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // redisTemplate.setKeySerializer(new StringRedisSerializer());                    // key序列化
        // redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());     // value序列化
        // redisTemplate.setHashKeySerializer(new StringRedisSerializer());                // Hash key序列化
        // redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer()); // Hash value序列化
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
