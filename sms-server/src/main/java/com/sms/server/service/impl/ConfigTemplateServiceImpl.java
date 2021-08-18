package com.sms.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.entity.ConfigTemplateEntity;
import com.sms.mapper.ConfigTemplateMapper;
import com.sms.server.service.ConfigTemplateService;
import org.springframework.stereotype.Service;

/**
 * 配置—模板表
 */
@Service
public class ConfigTemplateServiceImpl extends ServiceImpl<ConfigTemplateMapper, ConfigTemplateEntity> implements ConfigTemplateService {

}
