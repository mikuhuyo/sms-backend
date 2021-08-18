package com.sms.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.api.service.PlatformService;
import com.sms.entity.PlatformEntity;
import com.sms.mapper.PlatformMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 平台
 */
@Service
public class PlatformServiceImpl extends ServiceImpl<PlatformMapper, PlatformEntity> implements PlatformService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PlatformEntity getByAccessKeyId(String accessKeyId) {

        ValueOperations<String, PlatformEntity> ops = redisTemplate.opsForValue();
        PlatformEntity platform = ops.get(accessKeyId);
        if (platform == null) {
            LambdaQueryWrapper<PlatformEntity> wrapper = new LambdaQueryWrapper();
            wrapper.eq(PlatformEntity::getAccessKeyId, accessKeyId);
            platform = baseMapper.selectOne(wrapper);
            ops.set(accessKeyId, platform, 60, TimeUnit.SECONDS);
        }
        return platform;
    }
}
