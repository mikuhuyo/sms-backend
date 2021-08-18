package com.sms.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.dto.ConfigDTO;
import com.sms.entity.ConfigSignatureEntity;
import com.sms.manage.service.ConfigSignatureService;
import com.sms.mapper.ConfigSignatureMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 配置—签名表
 */
@Service
@Slf4j
public class ConfigSignatureServiceImpl extends ServiceImpl<ConfigSignatureMapper, ConfigSignatureEntity> implements ConfigSignatureService {

    @Override
    public void merge(ConfigDTO entity) {
        if (!CollectionUtils.isEmpty(entity.getSignatureIds())) {
            LambdaQueryWrapper<ConfigSignatureEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConfigSignatureEntity::getConfigId, entity.getId());

            // 数据库中的关联项
            List<ConfigSignatureEntity> dbList = this.list(wrapper);
            List<String> dbSignatureIds = dbList.stream().map(item -> item.getSignatureId()).collect(Collectors.toList());
            // 删除
            List<String> deleteIds = dbSignatureIds.stream().filter(item -> !entity.getSignatureIds().contains(item)).collect(Collectors.toList());
            // 新增
            List<String> addIds = entity.getSignatureIds().stream().filter(item -> !dbSignatureIds.contains(item)).collect(Collectors.toList());


            if (!CollectionUtils.isEmpty(deleteIds)) {
                wrapper.in(ConfigSignatureEntity::getSignatureId, deleteIds);
                this.remove(wrapper);
                log.info("删除成功 config:{} deleteIds:{}", entity.getId(), deleteIds);
            }
            if (!CollectionUtils.isEmpty(addIds)) {
                List<ConfigSignatureEntity> configSignatureEntities = addIds.stream().map(item -> {
                    ConfigSignatureEntity configSignatureEntity = new ConfigSignatureEntity();
                    configSignatureEntity.setConfigId(entity.getId());
                    configSignatureEntity.setSignatureId(item);
                    return configSignatureEntity;
                }).collect(Collectors.toList());
                this.saveBatch(configSignatureEntities);
                log.info("新增成功 config:{} addIds:{}", entity.getId(), addIds);
            }
        }
    }
}
