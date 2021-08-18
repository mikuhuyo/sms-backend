package com.sms.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.entity.ConfigSignatureEntity;
import com.sms.entity.SignatureEntity;
import com.sms.mapper.ConfigSignatureMapper;
import com.sms.mapper.SignatureMapper;
import com.sms.server.service.SignatureService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 签名表
 */
@Service
public class SignatureServiceImpl extends ServiceImpl<SignatureMapper, SignatureEntity> implements SignatureService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ConfigSignatureMapper configSignatureMapper;

    @Override
    public SignatureEntity getByCode(String signature) {
        ValueOperations<String, SignatureEntity> ops = redisTemplate.opsForValue();
        SignatureEntity signatureEntity = ops.get(signature);
        if (signatureEntity == null) {
            LambdaQueryWrapper<SignatureEntity> wrapper = new LambdaQueryWrapper();
            wrapper.eq(SignatureEntity::getCode, signature);
            signatureEntity = baseMapper.selectOne(wrapper);
            ops.set(signature, signatureEntity, 60, TimeUnit.SECONDS);
        }
        return signatureEntity;
    }

    @Override
    public String getConfigCodeByCode(String id, String signature) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String signatureCode = ops.get(signature + "_" + id + "_code");
        if (StringUtils.isBlank(signatureCode)) {
            LambdaQueryWrapper<SignatureEntity> signatureWrapper = new LambdaQueryWrapper<>();
            signatureWrapper.eq(SignatureEntity::getCode, signature);
            SignatureEntity signatureEntity = baseMapper.selectOne(signatureWrapper);

            LambdaQueryWrapper<ConfigSignatureEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConfigSignatureEntity::getConfigId, id);
            wrapper.eq(ConfigSignatureEntity::getSignatureId, signatureEntity.getId());
            ConfigSignatureEntity configSignatureEntity = configSignatureMapper.selectOne(wrapper);
            signatureCode = configSignatureEntity != null ? configSignatureEntity.getConfigSignatureCode() : "";
            ops.set(signature + "_" + id + "_code", signatureCode, 60, TimeUnit.SECONDS);
        }
        return signatureCode;
    }
}
