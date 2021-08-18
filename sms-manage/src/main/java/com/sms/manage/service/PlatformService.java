package com.sms.manage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sms.entity.PlatformEntity;

/**
 * 接入平台
 */
public interface PlatformService extends IService<PlatformEntity> {

    PlatformEntity getByName(String name);
}
