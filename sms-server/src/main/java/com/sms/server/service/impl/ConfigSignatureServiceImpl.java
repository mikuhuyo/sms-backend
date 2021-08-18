package com.sms.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.entity.ConfigSignatureEntity;
import com.sms.mapper.ConfigSignatureMapper;
import com.sms.server.service.ConfigSignatureService;
import org.springframework.stereotype.Service;

/**
 * 配置—签名表
 */
@Service
public class ConfigSignatureServiceImpl extends ServiceImpl<ConfigSignatureMapper, ConfigSignatureEntity> implements ConfigSignatureService {


}
