package com.sms.manage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sms.dto.ConfigDTO;
import com.sms.entity.ConfigSignatureEntity;

/**
 * 配置—签名表
 */
public interface ConfigSignatureService extends IService<ConfigSignatureEntity> {

    void merge(ConfigDTO entity);
}
