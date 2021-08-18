package com.sms.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pd.core.utils.DateUtils;
import com.sms.dto.ConfigDTO;
import com.sms.entity.ConfigEntity;
import com.sms.entity.SendLogEntity;
import com.sms.mapper.ConfigMapper;
import com.sms.mapper.SendLogMapper;
import com.sms.server.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 通道配置表
 */
@Service
@Slf4j
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigEntity> implements ConfigService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SendLogMapper sendLogMapper;

    /**
     * 获取Redis中的可用通道, 如果redis中没有, 从数据库中查找
     *
     * @return
     */
    @Override
    public List<ConfigEntity> listForConnect() {
        //获取Redis的通道列表
        ValueOperations<String, List<ConfigEntity>> ops = redisTemplate.opsForValue();
        List<ConfigEntity> configEntities = ops.get("listForConnect");
        log.info("listForConnect value for cache: {}", configEntities);

        //如果Redis中不存在, 查询数据库
        if (CollectionUtils.isEmpty(configEntities)) {
            LambdaQueryWrapper<ConfigEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConfigEntity::getChannelType, 1);
            wrapper.eq(ConfigEntity::getIsActive, 1);
            wrapper.eq(ConfigEntity::getIsEnable, 1);
            wrapper.orderByAsc(ConfigEntity::getLevel);
            configEntities = super.list(wrapper);
            log.info("listForConnect value for db: {}", configEntities);
            ops.set("listForConnect", configEntities, 60, TimeUnit.SECONDS);
        }
        return configEntities;
    }

    /**
     * 通道选举, 选举策略:
     * 1.剔除掉第一级通道
     * 2.查询最近一小时内通道发送短信数量, 按数量排序通道
     * 3.如果最近一小时没有发送短信, 按最后发送成功排序
     *
     * @return
     */
    @Override
    public List<ConfigEntity> listForNewConnect() {
        // 1.获取全部配置
        List<ConfigEntity> configs = this.listForConnect();

        //2.降级第一级别通道
        Iterator<ConfigEntity> it = configs.iterator();
        ConfigEntity firstConfigEntity = null;
        if (it.hasNext()) {
            firstConfigEntity = it.next();
            log.info("当前第一级别通道:{}", firstConfigEntity);
        }

        // 3 排除第一级别配置, 查询可用通道配置
        LambdaQueryWrapper<ConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(ConfigEntity::getId, firstConfigEntity.getId());
        queryWrapper.eq(ConfigEntity::getIsActive, 1);
        queryWrapper.orderByAsc(ConfigEntity::getLevel);
        List<ConfigEntity> list = baseMapper.selectList(queryWrapper);
        log.info("全部开启通道:{}", list);


        //获取当前时间前一个小时的时间
        List<ConfigDTO> configDTOS = new ArrayList<>();
        Date date = new Date();//获取当前时间    
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        Date finalDate = calendar.getTime();

        //通道是否发送过短信标识
        AtomicBoolean finded = new AtomicBoolean(false);

        //4. 记录每个可用通道最近一个小时内的发送成功的次数
        list.forEach(configEntity -> {
            ConfigDTO configDTO = new ConfigDTO();
            BeanUtils.copyProperties(configEntity, configDTO);
            LambdaQueryWrapper<SendLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SendLogEntity::getConfigId, configEntity.getId());
            wrapper.ge(SendLogEntity::getCreateTime, DateUtils.formatAsDateTime(finalDate));
            wrapper.orderByDesc(SendLogEntity::getCreateTime);
            List<SendLogEntity> logList = sendLogMapper.selectList(wrapper);
            if (!CollectionUtils.isEmpty(logList)) {
                if (logList.get(0).getStatus() == 1) {
                    configDTO.setLastSuccessNumInAnHour(logList.size());
                    finded.set(true);
                }
            }
            configDTOS.add(configDTO);
        });

        /**
         * 5. 根据最近一小时内发送成功进行通道排序
         */
        configDTOS.sort(Comparator.comparing(ConfigDTO::getLastSuccessNumInAnHour, Collections.reverseOrder()).thenComparing(ConfigDTO::getLevel, Collections.reverseOrder()));
        log.info("第一轮排序后:{}", configDTOS);

        /**
         * 6. 如果最近一个小时内没有发送成功的通道, 执行以下排序方案:
         * 查找最后一次发送成功的通道, 进行排序
         */
        if (!finded.get()) {
            configDTOS.forEach(configDTO -> {
                LambdaQueryWrapper<SendLogEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SendLogEntity::getConfigId, configDTO.getId());
                wrapper.orderByDesc(SendLogEntity::getCreateTime);
                wrapper.last("LIMIT 1");
                SendLogEntity logEntity = sendLogMapper.selectOne(wrapper);
                if (logEntity != null) {
                    configDTO.setLastSuccessNum(1);
                }
            });
            //次数相同, 按级别排序
            configDTOS.sort(Comparator.comparing(ConfigDTO::getLastSuccessNum, Collections.reverseOrder()).thenComparing(ConfigDTO::getLevel));
            log.info("第二轮排序后:{}", configDTOS);
        }

        int level = 1;
        list.clear();
        for (ConfigDTO configDTO : configDTOS) {
            ConfigEntity configEntity = new ConfigEntity();
            BeanUtils.copyProperties(configDTO, configEntity);
            configEntity.setLevel(level++);
            list.add(configEntity);
        }

        // 查询不可用通道
        LambdaQueryWrapper<ConfigEntity> unActivequeryWrapper = new LambdaQueryWrapper<>();
        unActivequeryWrapper.ne(ConfigEntity::getId, firstConfigEntity.getId());
        unActivequeryWrapper.eq(ConfigEntity::getIsActive, 0);
        unActivequeryWrapper.orderByAsc(ConfigEntity::getLevel);
        List<ConfigEntity> unActiveList = baseMapper.selectList(unActivequeryWrapper);

        for (ConfigEntity configEntity : unActiveList) {
            configEntity.setLevel(level++);
            list.add(configEntity);
        }

        // 原第一通道置位不可用 并将排序推后
        firstConfigEntity.setLevel(99);
        firstConfigEntity.setIsEnable(0);
        list.add(firstConfigEntity);

        log.info("listForNewConnect value: {}", list);
        return list;
    }

    @Override
    public boolean updateBatchById(Collection<ConfigEntity> entityList) {
        boolean result = super.updateBatchById(entityList);
        redisTemplate.delete("listForConnect");
        return result;
    }

}
