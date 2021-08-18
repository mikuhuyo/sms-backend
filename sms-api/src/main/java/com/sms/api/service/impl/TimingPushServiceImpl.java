package com.sms.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.api.service.TimingPushService;
import com.sms.entity.TimingPushEntity;
import com.sms.mapper.TimingPushMapper;
import org.springframework.stereotype.Service;

/**
 * 定时发送
 */
@Service
public class TimingPushServiceImpl extends ServiceImpl<TimingPushMapper, TimingPushEntity> implements TimingPushService {

}
