package com.sms.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pd.core.base.R;
import com.pd.core.utils.DateUtils;
import com.sms.entity.*;
import com.sms.server.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 短信发送接口
 * header 中存放鉴权信息.平台信息
 * body 中只有短信内容
 */
@RestController
@RequestMapping("sms")
@Api(tags = "短信")
@Slf4j
public class SmsSendController {

    @Autowired
    private SignatureService signatureService;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private PlatformService platformService;

    @Autowired
    private ReceiveLogService receiveLogService;
    @Autowired
    private SendLogService sendLogService;

    private List<SignatureEntity> signatureEntities;
    private List<TemplateEntity> templateEntities;
    private List<ConfigEntity> configEntities;
    private List<PlatformEntity> platformEntities;

    private SignatureEntity signatureEntity;
    private TemplateEntity templateEntity;
    private ConfigEntity configEntity;
    private PlatformEntity platformEntity;

    @PostMapping("simulation")
    @ApiOperation("模拟发送")
    public R send(@RequestParam("date") String date, @RequestParam Integer num) {
        log.info("模拟发送 date:{}", date);
        if (num <= 0) {
            num = 1000;
        }
        Random rand = new Random();
        int count = rand.nextInt(num) + 1000;
        int fail = rand.nextInt(100);
        int success = count - fail;
        init();
        load();

        for (int i = 0; i < success; i++) {
            if (i % 9 == 0) {
                load();
            }
            String apiLogId = UUID.randomUUID().toString();
            ReceiveLogEntity receiveLogEntity = new ReceiveLogEntity();
            receiveLogEntity.setPlatformId(platformEntity.getId());
            receiveLogEntity.setPlatformName(platformEntity.getName());
            receiveLogEntity.setBusiness("simulation");
            receiveLogEntity.setConfigIds(configEntity.getId());
            receiveLogEntity.setTemplate(templateEntity.getCode());
            receiveLogEntity.setSignature(signatureEntity.getCode());
            receiveLogEntity.setMobile("18" + (rand.nextInt(900000000) + 100000000));
            receiveLogEntity.setRequest("{\"code\":\"9009\"}");
            receiveLogEntity.setUseTime(Long.valueOf(rand.nextInt(50)));
            receiveLogEntity.setStatus(1);
            receiveLogEntity.setApiLogId(apiLogId);
            receiveLogEntity.setCreateTime(DateUtils.date2LocalDateTime(DateUtils.parseAsDate(date)));
            receiveLogEntity.setUpdateTime(receiveLogEntity.getCreateTime());
            receiveLogEntity.setCreateUser("simulation");
            receiveLogEntity.setUpdateUser("simulation");
            receiveLogService.save(receiveLogEntity);

            SendLogEntity sendLogEntity = new SendLogEntity();
            sendLogEntity.setConfigId(configEntity.getId());
            sendLogEntity.setConfigPlatform(configEntity.getPlatform());
            sendLogEntity.setConfigName(configEntity.getName());
            sendLogEntity.setTemplate(templateEntity.getCode());
            sendLogEntity.setSignature(signatureEntity.getCode());
            sendLogEntity.setMobile(receiveLogEntity.getMobile());
            sendLogEntity.setUseTime(Long.valueOf(rand.nextInt(50)));
            sendLogEntity.setStatus(1);
            sendLogEntity.setRequest(receiveLogEntity.getRequest());
            sendLogEntity.setResponse("{\"Message\":\"OK\",\"RequestId\":\"C56B73A4-6C90-4903-A969-008412C18144\",\"BizId\":\"539516503785026866^0\",\"Code\":\"OK\"}");
            sendLogEntity.setApiLogId(apiLogId);
            sendLogEntity.setCreateTime(DateUtils.date2LocalDateTime(DateUtils.parseAsDate(date)));
            sendLogEntity.setUpdateTime(receiveLogEntity.getCreateTime());
            sendLogEntity.setCreateUser("simulation");
            sendLogEntity.setUpdateUser("simulation");
            sendLogService.save(sendLogEntity);

        }

        for (int i = 0; i < fail; i++) {
            if (i % 9 == 0) {
                load();
            }
            String apiLogId = UUID.randomUUID().toString();
            ReceiveLogEntity receiveLogEntity = new ReceiveLogEntity();
            receiveLogEntity.setPlatformId(platformEntity.getId());
            receiveLogEntity.setPlatformName(platformEntity.getName());
            receiveLogEntity.setBusiness("simulation");
            receiveLogEntity.setConfigIds(configEntity.getId());
            receiveLogEntity.setTemplate(templateEntity.getCode());
            receiveLogEntity.setSignature(signatureEntity.getCode());
            receiveLogEntity.setMobile("18" + (rand.nextInt(900000000) + 100000000));
            receiveLogEntity.setRequest("{\"code\":\"9009\"}");
            receiveLogEntity.setError("其他异常");
            receiveLogEntity.setUseTime(Long.valueOf(rand.nextInt(50)));
            receiveLogEntity.setStatus(0);
            receiveLogEntity.setApiLogId(apiLogId);
            receiveLogEntity.setCreateTime(DateUtils.date2LocalDateTime(DateUtils.parseAsDate(date)));
            receiveLogEntity.setUpdateTime(receiveLogEntity.getCreateTime());
            receiveLogEntity.setCreateUser("simulation");
            receiveLogEntity.setUpdateUser("simulation");
            receiveLogService.save(receiveLogEntity);

            SendLogEntity sendLogEntity = new SendLogEntity();
            sendLogEntity.setConfigId(configEntity.getId());
            sendLogEntity.setConfigPlatform(configEntity.getPlatform());
            sendLogEntity.setConfigName(configEntity.getName());
            sendLogEntity.setTemplate(templateEntity.getCode());
            sendLogEntity.setSignature(signatureEntity.getCode());
            sendLogEntity.setMobile(receiveLogEntity.getMobile());
            sendLogEntity.setUseTime(Long.valueOf(rand.nextInt(50)));
            sendLogEntity.setStatus(0);
            sendLogEntity.setRequest(receiveLogEntity.getRequest());
            sendLogEntity.setResponse("");
            sendLogEntity.setError("费用不足");
            sendLogEntity.setApiLogId(apiLogId);
            sendLogEntity.setCreateTime(DateUtils.date2LocalDateTime(DateUtils.parseAsDate(date)));
            sendLogEntity.setUpdateTime(receiveLogEntity.getCreateTime());
            sendLogEntity.setCreateUser("simulation");
            sendLogEntity.setUpdateUser("simulation");
            sendLogService.save(sendLogEntity);

        }

        return R.success().put("count", count).put("success", success).put("fail", fail);
    }


    private void init() {
        signatureEntities = signatureService.list();
        templateEntities = templateService.list();

        LambdaQueryWrapper<ConfigEntity> configWrapper = new LambdaQueryWrapper<>();
        configWrapper.eq(ConfigEntity::getIsEnable, 1);
        configWrapper.eq(ConfigEntity::getIsActive, 1);
        configEntities = configService.list(configWrapper);


        LambdaQueryWrapper<PlatformEntity> platformWrapper = new LambdaQueryWrapper<>();
        platformWrapper.eq(PlatformEntity::getIsActive, 1);
        platformEntities = platformService.list(platformWrapper);
    }

    private void load() {
        Random rand = new Random();
        signatureEntity = signatureEntities.get(rand.nextInt(signatureEntities.size()));
        templateEntity = templateEntities.get(rand.nextInt(templateEntities.size()));
        configEntity = configEntities.get(rand.nextInt(configEntities.size()));
        platformEntity = platformEntities.get(rand.nextInt(platformEntities.size()));
    }
}
