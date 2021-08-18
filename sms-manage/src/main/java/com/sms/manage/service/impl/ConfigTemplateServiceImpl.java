package com.sms.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.dto.ConfigDTO;
import com.sms.entity.ConfigTemplateEntity;
import com.sms.manage.service.ConfigTemplateService;
import com.sms.mapper.ConfigTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 配置—模板表
 */
@Service
@Slf4j
public class ConfigTemplateServiceImpl extends ServiceImpl<ConfigTemplateMapper, ConfigTemplateEntity> implements ConfigTemplateService {

    @Override
    public void merge(ConfigDTO entity) {
        if (!CollectionUtils.isEmpty(entity.getTemplateIds())) {
            LambdaQueryWrapper<ConfigTemplateEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConfigTemplateEntity::getConfigId, entity.getId());

            // 数据库中的关联项
            List<ConfigTemplateEntity> dbList = this.list(wrapper);
            List<String> dbTemplateIds = dbList.stream().map(item -> item.getTemplateId()).collect(Collectors.toList());
            // 删除
            List<String> deleteIds = dbTemplateIds.stream().filter(item -> !entity.getTemplateIds().contains(item)).collect(Collectors.toList());
            // 新增
            List<String> addIds = entity.getTemplateIds().stream().filter(item -> !dbTemplateIds.contains(item)).collect(Collectors.toList());


            if (!CollectionUtils.isEmpty(deleteIds)) {
                wrapper.in(ConfigTemplateEntity::getTemplateId, deleteIds);
                this.remove(wrapper);
                log.info("删除成功 config:{} deleteIds:{}", entity.getId(), deleteIds);
            }
            if (!CollectionUtils.isEmpty(addIds)) {
                List<ConfigTemplateEntity> configTemplateEntities = addIds.stream().map(item -> {
                    ConfigTemplateEntity configTemplateEntity = new ConfigTemplateEntity();
                    configTemplateEntity.setConfigId(entity.getId());
                    configTemplateEntity.setTemplateId(item);
                    return configTemplateEntity;
                }).collect(Collectors.toList());
                this.saveBatch(configTemplateEntities);
                log.info("新增成功 config:{} addIds:{}", entity.getId(), addIds);
            }
        }
    }
}
