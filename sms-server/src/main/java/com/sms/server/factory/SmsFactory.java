package com.sms.server.factory;

import com.alibaba.fastjson.JSON;
import com.sms.dto.SmsSendDTO;
import com.sms.entity.ManualProcessEntity;
import com.sms.entity.SendLogEntity;
import com.sms.server.properties.SmsProperties;
import com.sms.server.service.ManualProcessService;
import com.sms.server.service.SendLogService;
import com.sms.server.sms.AbstractSmsService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 短信发送工厂
 * 1. 获取构建好的短信通道
 * 2. 调用通道方法, 发送短信
 * 3. 如果发送出现异常, 触发通道选举和通道降级策略
 * 4. 当通道选举被触发时: smsConnectLoader.buildNewConnect()
 * 5. 当通道降级被触发时: smsConnectLoader.changeNewConnectMessage()
 * 6. 记录短信发送日志
 */
@Component
@Slf4j
public class SmsFactory {

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SendLogService sendLogService;

    @Autowired
    private ManualProcessService manualProcessService;

    @Autowired
    private SmsConnectLoader smsConnectLoader;

    /**
     * 根据级别获取当前通道
     *
     * @return
     */
    @SneakyThrows
    public AbstractSmsService getSmsServiceByLevel(Integer level) {

        return smsConnectLoader.getConnectByLevel(level);
    }

    /**
     * 根据级别获取通道id
     *
     * @param level
     * @return
     */
    public String getConfigIdByLevel(Integer level) {
        AbstractSmsService connect = smsConnectLoader.getConnectByLevel(level);
        return connect.getConfig().getId();
    }

    /**
     * 发送短信
     *
     * @param deserialize
     * @return
     */
    public boolean send(String deserialize) {
        Integer level = 1;
        Integer messageErrorNum = 0;
        do {
            log.info("发送短信 level:{} , json:{}", level, deserialize);
            SendLogEntity sendLog = new SendLogEntity();
            sendLog.setCreateTime(LocalDateTime.now());
            sendLog.setUpdateTime(sendLog.getCreateTime());
            long begin = System.currentTimeMillis();
            try {
                SmsSendDTO smsSendDTO = JSON.parseObject(deserialize, SmsSendDTO.class);
                AbstractSmsService abstractSmsService = null;

                /**
                 * 当所有通道全部尝试后, 如果通道级别大于所有通道配置的级别,
                 * 则说明所有通道都发送失败, 该短信需要人工处理
                 */
                if (smsConnectLoader.checkConnectLevel(level)) {
                    log.warn("短信发送失败, 需要人工介入处理");
                    ManualProcessEntity manualProcessEntity = new ManualProcessEntity();
                    manualProcessEntity.setMobile(smsSendDTO.getMobile());
                    manualProcessEntity.setSignature(smsSendDTO.getSignature());
                    manualProcessEntity.setTemplate(smsSendDTO.getTemplate());
                    manualProcessEntity.setConfigIds(StringUtils.join(smsSendDTO.getConfigIds()));
                    manualProcessEntity.setRequest(JSON.toJSONString(smsSendDTO.getParams()));
                    manualProcessEntity.setRequest(smsSendDTO.getSendTime());
                    manualProcessEntity.setCreateTime(LocalDateTime.now());
                    manualProcessService.save(manualProcessEntity);

                    sendLog.setConfigId("404");
                    sendLog.setConfigName("未找到");
                    sendLog.setConfigPlatform("未找到");
                    sendLog.setMobile(smsSendDTO.getMobile());
                    sendLog.setSignature(smsSendDTO.getSignature());
                    sendLog.setTemplate(smsSendDTO.getTemplate());
                    sendLog.setRequest(JSON.toJSONString(smsSendDTO));
                    sendLog.setApiLogId(smsSendDTO.getLogId());
                    sendLog.setStatus(0);
                    sendLog.setResponse("@未找到合适配置, 需人工处理");
                    return false;   //记录当前短信发送失败, 跳出循环
                }

                //根据级别获取通道id
                String configId = getConfigIdByLevel(level);


                /**
                 * 1.获取可用通道
                 *
                 * 发送短信需要模板和签名, 需要和通道绑定
                 * smsSendDTO.getConfigIds():  保存了可用模板和签名的通道列表
                 * 如果这个通道列表中包含当前通道id, 则表明当前通道是可以发送这条短信的通道
                 */
                if (smsSendDTO.getConfigIds().contains(configId)) {
                    /**
                     * 当前级别通道 符合 短信模板+签名支持的通达
                     * 此方法获取的是根据配置实例化的通道连接对象
                     */
                    abstractSmsService = getSmsServiceByLevel(level);

                    log.info("获取到通道: {},{}", abstractSmsService.getClass().getName(), level);
                    if (null == abstractSmsService) {
                        // 通道为空 获取下一级别通道
                        log.info("通道为空 获取下一级别通道 :{}", level);
                        level++;
                        continue;
                    }
                } else {
                    log.info("当前级别不符合:{} 查找下一级别 :{}", configId, level);
                    level++;
                    continue;
                }

                /**
                 * 已找到可用通道:
                 * 2.构建日志对象
                 */
                sendLog.setConfigId(abstractSmsService.getConfig().getId());
                sendLog.setConfigName(abstractSmsService.getConfig().getName());
                sendLog.setConfigPlatform(abstractSmsService.getConfig().getPlatform());
                sendLog.setMobile(smsSendDTO.getMobile());
                sendLog.setSignature(smsSendDTO.getSignature());
                sendLog.setTemplate(smsSendDTO.getTemplate());
                sendLog.setRequest(JSON.toJSONString(smsSendDTO));
                sendLog.setApiLogId(smsSendDTO.getLogId());
                sendLog.setStatus(1);

                /**
                 * 3.发送短信
                 * 获得通道返回的发送状态
                 */
                String response = abstractSmsService.send(smsSendDTO.getMobile(), smsSendDTO.getParams(), smsSendDTO.getSignature(), smsSendDTO.getTemplate());

                /**
                 * 4.检查发送结果
                 */
                sendLog.checkResponse(response);
                // 发送成功
                log.info("发送成功: {}", response);
                return true;
            } catch (Exception e) {
                log.warn("发送异常 返回值: {}", sendLog.getResponse(), e);
                sendLog.setStatus(0);
                sendLog.setError(getExceptionMessage(e));

                /**
                 *
                 * 5.重新排序通道:
                 *
                 * 检查通道失败次数是否超过阈值或一定比例,
                 * 如果失败次数超过阈值, 则降级通道, 通道重新排序
                 * 如果失败次数超过一定比例则启动新通道备用
                 */
                if (resetChannel(level)) {
                    level = 1;
                    continue;
                }

                /**
                 * 6.通道热切换
                 * 如果当前短信重试次数超过阈值, 则切换下一级别通道
                 */
                if (messageErrorNum >= smsProperties.getMessageErrorNum()) {
                    //短信单通道失败次数达到阈值
                    messageErrorNum = 0;
                    // 切换下一级别通道
                    level++;
                    log.info("短信单通道失败次数达到阈值 切换下一级别通道");
                } else {
                    //短信单通道失败次数尚未达到阈值
                    messageErrorNum++;
                    log.info("短信单通道失败次数尚未达到阈值");
                }
            } finally {
                if (StringUtils.isNotBlank(sendLog.getConfigId())) {
                    long end = System.currentTimeMillis();
                    sendLog.setUseTime(end - begin);
//                    sendLog.buildRemark();
                    //保存日志
                    sendLogService.save(sendLog);
                }
            }

        } while (true);
    }


    /**
     * 功能:  触发通道选举
     * <p>
     * 判断当前级别通道的失败次数是否大于阈值
     * 如果大于阈值, 则重新排序通道
     * 否则, 判断失败次数是否大于阈值的一定百分比（配置中的百分比）时, 则构建新的通道连接, 供后续使用
     * 注意: 此失败次数指的是固定时间内的失败次数, 当前设置为10分钟
     *
     * @param level
     */
    private boolean resetChannel(Integer level) {
        //获取各级别通道的失败次数
        ValueOperations<String, Integer> ops = redisTemplate.opsForValue();
        Integer configLevelFailNum = ops.get("config_level_" + level);
        if (configLevelFailNum == null) {
            configLevelFailNum = 0;
        }

        if (configLevelFailNum >= smsProperties.getConfigLevelFailNum()) {
            // 不需要清空, 等待超时即可
            // 当前通道失败次数大于 固定阈值时  重新排序通道
            log.info("当前通道失败次数大于 固定阈值时  重新排序通道 changeNewConnectMessage");
            smsConnectLoader.changeNewConnectMessage(); //通道降级
            return true;
        } else {
            if (configLevelFailNum >= (smsProperties.getConfigLevelFailNum() * smsProperties.getConfigBuildScale())) {
                //   当前通道失败次数大于 固定阈值的固定因子时  执行预排序通道  异步启动
                log.info("当前通道失败次数大于 固定阈值的固定因子时  执行预排序通道  异步启动 buildNewConnect");

                //通道选举, 调用方法: com.pd.service.impl.ConfigServiceImpl.listForNewConnect
                smsConnectLoader.buildNewConnect();     //通道选举
            }
            // 设置通道失败次数异常时间 10分钟, 代表十分钟内失败次数达到阈值就会被切换掉
            ops.set("config_level_" + level, configLevelFailNum + 1, 10, TimeUnit.MINUTES);
        }
        return false;
    }

    /**
     * 输出异常信息
     *
     * @param e
     * @return
     */
    private String getExceptionMessage(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter printWriter = new PrintWriter(sw);
        e.printStackTrace(printWriter);
        return sw.toString();
    }
}
