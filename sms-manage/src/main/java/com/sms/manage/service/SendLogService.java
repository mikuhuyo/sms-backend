package com.sms.manage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sms.entity.SendLogEntity;
import com.sms.vo.MarketingStatisticsCountVO;
import com.sms.vo.SendLogPageVO;
import com.sms.vo.SendLogVO;
import com.sms.vo.StatisticsCountVO;

import java.util.List;
import java.util.Map;

/**
 * 日志表
 */
public interface SendLogService extends IService<SendLogEntity> {

    Page<SendLogVO> pageLog(Page<SendLogVO> page, Map<String, Object> params);

    List<StatisticsCountVO> trend(Map params);

    Page<StatisticsCountVO> countPage(Page<StatisticsCountVO> page, Map<String, Object> params);

    List<Map> countForConfig(Map params);

    List<Map> rateForConfig(Map params);

    MarketingStatisticsCountVO getMarketingCountByBusinessId(String id);

    Page<SendLogPageVO> sendLogPage(Page<SendLogPageVO> page, SendLogPageVO sendLogPageVO);
}
