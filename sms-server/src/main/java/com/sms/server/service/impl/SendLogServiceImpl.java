package com.sms.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.entity.ReceiveLogEntity;
import com.sms.entity.SendLogEntity;
import com.sms.mapper.ReceiveLogMapper;
import com.sms.mapper.SendLogMapper;
import com.sms.server.service.SendLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 日志表
 */
@Service
public class SendLogServiceImpl extends ServiceImpl<SendLogMapper, SendLogEntity> implements SendLogService {

    @Autowired
    private ReceiveLogMapper receiveLogMapper;

    @Override
    public boolean save(SendLogEntity entity) {

        ReceiveLogEntity receiveLogEntity = new ReceiveLogEntity();
        receiveLogEntity.setStatus(entity.getStatus());
        LambdaUpdateWrapper<ReceiveLogEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ReceiveLogEntity::getApiLogId, entity.getApiLogId());
        receiveLogMapper.update(receiveLogEntity, wrapper);

        return super.save(entity);
    }
}
