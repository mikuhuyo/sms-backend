package com.sms.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.api.service.BlackListService;
import com.sms.entity.BlackListEntity;
import com.sms.mapper.BlackListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 黑名单
 */
@Service
public class BlackListServiceImpl extends ServiceImpl<BlackListMapper, BlackListEntity> implements BlackListService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<String> listByType(String type) {
        ValueOperations<String, List<String>> ops = redisTemplate.opsForValue();
        List<String> phones = ops.get("Black_" + type);
        if (CollectionUtils.isEmpty(phones)) {
            LambdaQueryWrapper<BlackListEntity> wrapper = new LambdaQueryWrapper();
            wrapper.eq(BlackListEntity::getType, type);
            List<BlackListEntity> blackListEntities = baseMapper.selectList(wrapper);
            phones = blackListEntities.stream().map(item -> item.getContent()).collect(Collectors.toList());
            if (null == phones) {
                phones = Collections.emptyList();
            }
            ops.set("Black_" + type, phones, 60, TimeUnit.SECONDS);
        }
        return phones;
    }
}
