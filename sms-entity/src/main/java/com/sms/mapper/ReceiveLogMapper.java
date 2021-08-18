package com.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sms.entity.ReceiveLogEntity;
import com.sms.vo.ReceiveLogVO;
import com.sms.vo.StatisticsCountVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 接收日志
 */
@Repository
public interface ReceiveLogMapper extends BaseMapper<ReceiveLogEntity> {

    IPage<ReceiveLogVO> selectLogPage(Page<ReceiveLogVO> page, @Param("params") Map<String, Object> params);

    List<StatisticsCountVO> top10(@Param("params") Map params);

    List<StatisticsCountVO> trend(@Param("params") Map params);
}
