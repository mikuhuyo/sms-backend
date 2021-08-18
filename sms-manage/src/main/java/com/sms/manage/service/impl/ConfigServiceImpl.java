package com.sms.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.dto.ConfigDTO;
import com.sms.entity.ConfigEntity;
import com.sms.manage.service.ConfigService;
import com.sms.mapper.ConfigMapper;
import com.sms.model.ServerTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 通道配置表
 */
@Service
@Slf4j
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigEntity> implements ConfigService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public ConfigEntity getByName(String name) {
        LambdaUpdateWrapper<ConfigEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ConfigEntity::getName, name);
        return this.getOne(wrapper);
    }

    @Override
    public void getNewLevel(ConfigDTO entity) {
        LambdaUpdateWrapper<ConfigEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ConfigEntity::getIsEnable, 1);
        wrapper.eq(ConfigEntity::getIsActive, 1);
        wrapper.orderByDesc(ConfigEntity::getLevel);
        wrapper.last("limit 1");
        ConfigEntity configEntity = this.getOne(wrapper);
        if (configEntity == null) {
            entity.setLevel(1);
        } else {
            entity.setLevel(configEntity.getLevel() + 1);
        }
    }

    @Override
    public void sendUpdateMessage() {
        Map map = redisTemplate.opsForHash().entries("SERVER_ID_HASH");
        log.info("全部服务: {}", map);
        Long current = System.currentTimeMillis();

        for (Object key : map.keySet()) {
            long valueLong = Long.parseLong(map.get(key).toString());
            if (current - valueLong < (1000 * 60 * 5)) {
                // 五分钟内报告
                redisTemplate.delete("listForConnect");
                redisTemplate.convertAndSend("TOPIC_HIGH_SERVER",
                        ServerTopic
                                .builder()
                                .option(ServerTopic.INIT_CONNECT)
                                .value(key.toString())
                                .build()
                                .toString()
                );
                log.info("找到可用服务执行构建通道: {}", map);
                return;
            }
        }
    }
}
