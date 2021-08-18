package com.sms.manage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sms.dto.ConfigDTO;
import com.sms.entity.ConfigTemplateEntity;

/**
 * 配置—模板表
 */
public interface ConfigTemplateService extends IService<ConfigTemplateEntity> {

    void merge(ConfigDTO entity);
}
