package com.sms.server.redismq;

import com.sms.server.factory.SmsFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Redis队列-----消费者
 * 监听消息队列: TOPIC_HIGH_SMS, 高优先级的短信, 如验证码之类的短信
 */
@Component
@Slf4j
public class HighSmsListener extends Thread {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SmsFactory smsFactory;

    private String queueKey = "TOPIC_HIGH_SMS";

    @Value("${spring.redis.queue.pop.timeout}")
    private Long popTimeout = 8000L;

    private ListOperations listOps;

    @PostConstruct
    private void init() {
        listOps = redisTemplate.opsForList();
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            log.debug("{} 监听中", queueKey);
            System.out.println("监听中..." + new Date());
            String message = (String) listOps.rightPop(queueKey, popTimeout, TimeUnit.MILLISECONDS);
            if (null != message) {
                log.info("{} 收到消息:{}", queueKey, message);
                smsFactory.send(message);
            }
        }
    }
}
