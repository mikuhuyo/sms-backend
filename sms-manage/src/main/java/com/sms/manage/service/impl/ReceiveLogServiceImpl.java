package com.sms.manage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.entity.ReceiveLogEntity;
import com.sms.manage.service.ReceiveLogService;
import com.sms.mapper.ReceiveLogMapper;
import com.sms.vo.ReceiveLogVO;
import com.sms.vo.StatisticsCountVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 接收日志表
 */
@Service
public class ReceiveLogServiceImpl extends ServiceImpl<ReceiveLogMapper, ReceiveLogEntity> implements ReceiveLogService {

    @Override
    public Page<ReceiveLogVO> pageLog(Page<ReceiveLogVO> page, Map<String, Object> params) {
        IPage<ReceiveLogVO> receiveLogVOPage = this.baseMapper.selectLogPage(page, params);
        page.setRecords(receiveLogVOPage.getRecords());
        return page;
    }

    @Override
    public List<StatisticsCountVO> top10(Map params) {
        return this.baseMapper.top10(params);
    }

    @Override
    public List<StatisticsCountVO> trend(Map params) {
        return this.baseMapper.trend(params);
    }

}
