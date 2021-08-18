package com.sms.server.redismq;

import com.sms.model.ServerTopic;
import com.sms.server.factory.SmsConnectLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

/**
 * Redis发布订阅----监听器（订阅者）
 */
@Component
@Slf4j
public class HighServerReceiver implements MessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SmsConnectLoader smsConnectLoader;

    /**
     * 消息监听
     *
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        RedisSerializer<?> valueSerializer = redisTemplate.getDefaultSerializer();
        String deserialize = valueSerializer.deserialize(message.getBody()).toString();
        log.info("收到的服务id消息: {}", deserialize);
        ServerTopic serverTopic = ServerTopic.load(deserialize);
        switch (serverTopic.getOption()) {
            // 应用通道连接池
            case ServerTopic.USE_NEW_CONNECT:
                log.info("服务: {} ,发起新连接应用", serverTopic.getValue());
                smsConnectLoader.changeNewConnect();
                break;
            // 创建通道连接池
            case ServerTopic.INIT_CONNECT:
                log.info("服务: {} ,发起新连接初始化", serverTopic.getValue());
                smsConnectLoader.initConnect();
                break;
            default:
                break;
        }
    }
}
