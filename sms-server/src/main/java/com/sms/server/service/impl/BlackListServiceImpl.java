package com.sms.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.entity.BlackListEntity;
import com.sms.mapper.BlackListMapper;
import com.sms.server.service.BlackListService;
import org.springframework.stereotype.Service;

/**
 * 黑名单
 */
@Service
public class BlackListServiceImpl extends ServiceImpl<BlackListMapper, BlackListEntity> implements BlackListService {


}
