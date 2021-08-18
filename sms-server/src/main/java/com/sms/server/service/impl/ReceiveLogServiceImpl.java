package com.sms.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.entity.ReceiveLogEntity;
import com.sms.mapper.ReceiveLogMapper;
import com.sms.server.service.ReceiveLogService;
import org.springframework.stereotype.Service;

/**
 * 接收日志表
 */
@Service
public class ReceiveLogServiceImpl extends ServiceImpl<ReceiveLogMapper, ReceiveLogEntity> implements ReceiveLogService {

}
