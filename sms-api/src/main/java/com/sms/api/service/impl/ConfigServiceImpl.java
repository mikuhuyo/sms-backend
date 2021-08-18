package com.sms.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.api.service.ConfigService;
import com.sms.entity.ConfigEntity;
import com.sms.mapper.ConfigMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通道配置
 */
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigEntity> implements ConfigService {

    @Override
    public List<ConfigEntity> findByTemplateSignature(String template, String signature) {

        Map params = new HashMap();
        params.put("template", template);
        params.put("signature", signature);

        return baseMapper.findByTemplateSignature(params);
    }
}
