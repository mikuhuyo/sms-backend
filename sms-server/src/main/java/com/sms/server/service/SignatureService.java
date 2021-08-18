package com.sms.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sms.entity.SignatureEntity;

/**
 * 签名表
 */
public interface SignatureService extends IService<SignatureEntity> {

    SignatureEntity getByCode(String signature);

    String getConfigCodeByCode(String id, String signature);
}
