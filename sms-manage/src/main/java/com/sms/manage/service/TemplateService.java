package com.sms.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sms.dto.TemplateDTO;
import com.sms.entity.TemplateEntity;

import java.util.List;
import java.util.Map;

/**
 * 模板表
 */
public interface TemplateService extends IService<TemplateEntity> {
    String getNextCode();

    IPage<TemplateDTO> customPage(Page<TemplateDTO> page, Map params);

    List<TemplateDTO> customList(Map params);

    TemplateEntity getByCode(String code);

    TemplateEntity getByName(String name);
}
