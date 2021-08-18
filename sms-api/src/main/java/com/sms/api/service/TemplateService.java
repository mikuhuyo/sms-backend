package com.sms.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sms.entity.TemplateEntity;

/**
 * 模板表
 */
public interface TemplateService extends IService<TemplateEntity> {

    TemplateEntity getByCode(String template);
}
