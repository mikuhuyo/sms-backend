package com.sms.server.config;

import com.sms.server.redismq.HighServerReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 订阅发布模式的容器配置
 */
@Configuration
@AutoConfigureAfter({HighServerReceiver.class})
public class SubscriberConfig {

    @Autowired
    private HighServerReceiver highServerReceiver;

    /**
     * 创建消息监听容器
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisMessageListenerContainer getRedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);

        //可以添加多个监听订阅通道
        //当前监听的是通道: TOPIC_HIGH_SERVER
        redisMessageListenerContainer.addMessageListener(new MessageListenerAdapter(highServerReceiver), new PatternTopic("TOPIC_HIGH_SERVER"));

        return redisMessageListenerContainer;
    }
}
