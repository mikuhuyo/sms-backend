package com.sms.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.entity.ConfigTemplateEntity;
import com.sms.entity.TemplateEntity;
import com.sms.mapper.ConfigTemplateMapper;
import com.sms.mapper.TemplateMapper;
import com.sms.server.service.TemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 模板表
 */
@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, TemplateEntity> implements TemplateService {
    @Autowired
    private ConfigTemplateMapper configTemplateMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public TemplateEntity getByCode(String template) {
        ValueOperations<String, TemplateEntity> ops = redisTemplate.opsForValue();
        TemplateEntity templateEntity = ops.get(template);
        if (templateEntity == null) {
            LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper();
            wrapper.eq(TemplateEntity::getCode, template);
            templateEntity = baseMapper.selectOne(wrapper);
            ops.set(template, templateEntity, 60, TimeUnit.SECONDS);
        }
        return templateEntity;
    }

    @Override
    public String getConfigCodeByCode(String id, String template) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String templateCode = ops.get(template + "_" + id + "_code");
        if (StringUtils.isBlank(templateCode)) {
            LambdaQueryWrapper<TemplateEntity> templateWrapper = new LambdaQueryWrapper<>();
            templateWrapper.eq(TemplateEntity::getCode, template);
            TemplateEntity templateEntity = baseMapper.selectOne(templateWrapper);
            LambdaQueryWrapper<ConfigTemplateEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConfigTemplateEntity::getConfigId, id);
            wrapper.eq(ConfigTemplateEntity::getTemplateId, templateEntity.getId());
            ConfigTemplateEntity configTemplateEntity = configTemplateMapper.selectOne(wrapper);
            templateCode = configTemplateEntity != null ? configTemplateEntity.getConfigTemplateCode() : "";
            ops.set(template + "_" + id + "_code", templateCode, 60, TimeUnit.SECONDS);
        }
        return templateCode;
    }
}
