package com.sms.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.entity.ManualProcessEntity;
import com.sms.mapper.ManualProcessMapper;
import com.sms.server.service.ManualProcessService;
import org.springframework.stereotype.Service;

/**
 * 人工处理任务表
 */
@Service
public class ManualProcessServiceImpl extends ServiceImpl<ManualProcessMapper, ManualProcessEntity> implements ManualProcessService {

}
