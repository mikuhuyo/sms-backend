package com.sms.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.dto.TemplateDTO;
import com.sms.entity.TemplateEntity;
import com.sms.entity.base.BaseEntity;
import com.sms.manage.service.TemplateService;
import com.sms.manage.utils.StringUtils;
import com.sms.mapper.ReceiveLogMapper;
import com.sms.mapper.TemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 模板表
 */
@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, TemplateEntity> implements TemplateService {

    @Autowired
    private ReceiveLogMapper receiveLogMapper;

    @Override
    public String getNextCode() {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BaseEntity::getCreateTime);
        wrapper.last(" limit 1");
        TemplateEntity entity = this.baseMapper.selectOne(wrapper);
        if (entity != null) {
            String code = entity.getCode();
            if (code.startsWith("DXMB")) {
                String numStr = code.split("_")[1];
                int num = Integer.parseInt(numStr) + 1;
                return "DXMB_" + StringUtils.autoGenericCode(num, 9);
            }
        }
        return "DXMB_000000001";
    }

    @Override
    public IPage<TemplateDTO> customPage(Page<TemplateDTO> page, Map params) {
        IPage<TemplateDTO> templateDTOIPage = this.baseMapper.custom(page, params);
        page.setRecords(templateDTOIPage.getRecords());
        return templateDTOIPage;
    }

    @Override
    public List<TemplateDTO> customList(Map params) {
        List<TemplateDTO> templateDTOS = this.baseMapper.custom(params);
        return templateDTOS;
    }

    @Override
    public TemplateEntity getByCode(String template) {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getCode, template);
        TemplateEntity entity = this.baseMapper.selectOne(wrapper);
        return entity;
    }

    @Override
    public TemplateEntity getByName(String name) {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getName, name);
        TemplateEntity entity = this.baseMapper.selectOne(wrapper);
        return entity;
    }
}
