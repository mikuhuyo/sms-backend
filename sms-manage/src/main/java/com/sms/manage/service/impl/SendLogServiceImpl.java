package com.sms.manage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.entity.SendLogEntity;
import com.sms.manage.service.SendLogService;
import com.sms.mapper.SendLogMapper;
import com.sms.vo.MarketingStatisticsCountVO;
import com.sms.vo.SendLogPageVO;
import com.sms.vo.SendLogVO;
import com.sms.vo.StatisticsCountVO;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 日志表
 */
@Service
public class SendLogServiceImpl extends ServiceImpl<SendLogMapper, SendLogEntity> implements SendLogService {

    @Override
    public Page<SendLogVO> pageLog(Page<SendLogVO> page, Map<String, Object> params) {
        IPage<SendLogVO> sendLogVOPage = this.baseMapper.selectLogPage(page, params);
        page.setRecords(sendLogVOPage.getRecords());
        return page;
    }

    @Override
    public List<StatisticsCountVO> trend(Map params) {
        return this.baseMapper.trend(params);
    }

    @Override
    public Page<StatisticsCountVO> countPage(Page<StatisticsCountVO> page, Map<String, Object> params) {
        IPage<StatisticsCountVO> countPage = this.baseMapper.countPage(page, params);
        countPage.getRecords().stream().map(item -> {
            item.setFail(item.getCount() - item.getSuccess());
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
            //可以设置精确几位小数
            df.setMaximumFractionDigits(2);
            //模式 例如四舍五入
            df.setRoundingMode(RoundingMode.HALF_UP);
            double accuracy_num = (double) item.getSuccess() / (double) item.getCount() * 100;
            item.setSuccessRate(df.format(accuracy_num));
            return item;
        }).collect(Collectors.toList());
        page.setRecords(countPage.getRecords());
        return page;
    }

    @Override
    public List<Map> countForConfig(Map params) {
        return this.baseMapper.countForConfig(params);
    }

    @Override
    public List<Map> rateForConfig(Map params) {
        List<Map> list = this.baseMapper.countForConfig(params);
        for (Map map : list) {
            int count = Integer.parseInt(map.get("count").toString());
            int success = Integer.parseInt(map.get("value").toString());
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
            //可以设置精确几位小数
            df.setMaximumFractionDigits(2);
            //模式 例如四舍五入
            df.setRoundingMode(RoundingMode.HALF_UP);
            double accuracy_num = (double) success / (double) count * 100;
            map.put("value", df.format(accuracy_num));
        }
        return list;
    }

    @Override
    public MarketingStatisticsCountVO getMarketingCountByBusinessId(String id) {
        Map params = new HashMap();
        params.put("business", id);
        MarketingStatisticsCountVO marketingStatisticsCountVO = this.baseMapper.getMarketingCount(params);
        marketingStatisticsCountVO.setFail(marketingStatisticsCountVO.getCount() - marketingStatisticsCountVO.getSuccess());
        if (marketingStatisticsCountVO.getCount() > 0) {
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
            //可以设置精确几位小数
            df.setMaximumFractionDigits(2);
            //模式 例如四舍五入
            df.setRoundingMode(RoundingMode.HALF_UP);
            double accuracy_num = (double) marketingStatisticsCountVO.getSuccess() / (double) marketingStatisticsCountVO.getCount() * 100;
            marketingStatisticsCountVO.setSuccessRate(df.format(accuracy_num));
        } else {
            marketingStatisticsCountVO.setSuccessRate("100");
        }

        return marketingStatisticsCountVO;
    }

    @Override
    public Page<SendLogPageVO> sendLogPage(Page<SendLogPageVO> page, SendLogPageVO sendLogPageVO) {
        IPage<SendLogPageVO> sendLogVOPage = this.baseMapper.sendLogPage(page, sendLogPageVO);
        page.setRecords(sendLogVOPage.getRecords());
        return page;
    }
}
