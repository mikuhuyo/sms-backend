package com.sms.manage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sms.entity.ReceiveLogEntity;
import com.sms.vo.ReceiveLogVO;
import com.sms.vo.StatisticsCountVO;

import java.util.List;
import java.util.Map;

/**
 * 接收日志表
 */
public interface ReceiveLogService extends IService<ReceiveLogEntity> {

    Page<ReceiveLogVO> pageLog(Page<ReceiveLogVO> page, Map<String, Object> params);

    List<StatisticsCountVO> top10(Map params);

    List<StatisticsCountVO> trend(Map params);
}
