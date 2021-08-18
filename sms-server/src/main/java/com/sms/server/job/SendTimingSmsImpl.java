package com.sms.server.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sms.entity.TimingPushEntity;
import com.sms.mapper.TimingPushMapper;
import com.sms.server.factory.SmsFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时发送短信
 */
@Component
@Slf4j
public class SendTimingSmsImpl implements SendTimingSms {

    @Autowired
    private TimingPushMapper timingPushMapper;

    @Autowired
    private SmsFactory smsFactory;

    @Override
    @Async
    public void execute(String timing) {
        log.info("任务开始执行" + timing);
        //查找需要发送的短信
        LambdaQueryWrapper<TimingPushEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimingPushEntity::getStatus, 0);
        wrapper.eq(TimingPushEntity::getTiming, timing);
        wrapper.orderByAsc(TimingPushEntity::getCreateTime, TimingPushEntity::getId);
        List<TimingPushEntity> timingPushList = timingPushMapper.selectList(wrapper);
        log.info("本次定时任务需处理条数:{}", timingPushList.size());
        timingPushList.forEach(timingPushEntity -> {
            String deserialize = timingPushEntity.getRequest();
            log.info("定时发送短信:{}", deserialize);
            //发送短信
            smsFactory.send(deserialize);
            timingPushEntity.setStatus(1);
            timingPushMapper.updateById(timingPushEntity);
        });

        log.info("任务执行完毕" + timing);
    }
}
