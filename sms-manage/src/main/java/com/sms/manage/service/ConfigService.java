package com.sms.manage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sms.dto.ConfigDTO;
import com.sms.entity.ConfigEntity;

/**
 * 配置表
 */
public interface ConfigService extends IService<ConfigEntity> {

    ConfigEntity getByName(String name);

    void getNewLevel(ConfigDTO entity);

    void sendUpdateMessage();
}
