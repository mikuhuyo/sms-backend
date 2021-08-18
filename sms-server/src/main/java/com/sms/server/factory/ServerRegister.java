package com.sms.server.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 服务选举器
 */
@Component
@Slf4j
@Order(value = 100)
public class ServerRegister implements CommandLineRunner {
    public static String SERVER_ID = null;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void run(String... args) {
        // 生成服务码
        SERVER_ID = UUID.randomUUID().toString();
        log.info("生成服务id:{}", SERVER_ID);
        // 注册服务id
        redisTemplate.opsForHash().put("SERVER_ID_HASH", SERVER_ID, System.currentTimeMillis());
    }

    /**
     * 定时服务报告
     * 报告服务信息证明服务存在 每三分钟报告一次, 并传入当前时间戳
     */
    @Scheduled(cron = "1 0/3 * * * ?")
    public void serverReport() {
        log.info("服务报告任务:{}", SERVER_ID);
        // 报告服务注册信息
        redisTemplate.opsForHash().put("SERVER_ID_HASH", SERVER_ID, System.currentTimeMillis());
    }

    /**
     * 定时服务检查
     * 每十分钟检查一次服务列表, 清空超过五分钟没有报告的服务
     */
    @Scheduled(cron = "30 0/10 * * * ?")
    public void checkServer() {
        log.info("服务检查任务:{}", SERVER_ID);
        // 报告服务注册信息
        Map map = redisTemplate.opsForHash().entries("SERVER_ID_HASH");
        log.info("服务检查: {}", map);
        Long current = System.currentTimeMillis();
        List removeKeys = new ArrayList();

        map.forEach((key, value) -> {
            long valueLong = Long.parseLong(value.toString());
            if (current - valueLong > (1000 * 60 * 5)) {
                // 已经超过五分钟没有报告
                removeKeys.add(key);
            }
        });

        log.info("服务检查 失效服务: {}", removeKeys);

        removeKeys.forEach(key -> {
            redisTemplate.opsForHash().delete("SERVER_ID_HASH", key);
        });
    }
}

