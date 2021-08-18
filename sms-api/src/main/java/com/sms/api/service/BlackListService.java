package com.sms.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sms.entity.BlackListEntity;

import java.util.List;

/**
 * 黑名单
 */
public interface BlackListService extends IService<BlackListEntity> {
    List<String> listByType(String s);
}
