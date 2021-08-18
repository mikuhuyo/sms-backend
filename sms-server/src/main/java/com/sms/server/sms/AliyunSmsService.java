package com.sms.server.sms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.pd.core.utils.DateUtils;
import com.sms.entity.SignatureEntity;
import com.sms.server.entity.SmsConfig;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 阿里云短信服务
 */
@Slf4j
public class AliyunSmsService extends AbstractSmsService {

    private IClientProfile profile;

    public AliyunSmsService(SmsConfig config) {
        this.config = config;
        //初始化
        init();
    }

    private void init() {
        //初始化acsClient, 暂不支持region化 "cn-hangzhou"
        profile = DefaultProfile.getProfile(config.get("RegionId"), config.getAccessKeyId(), config.getAccessKeySecret());
    }

    //https://help.aliyun.com/document_detail/55284.html?spm=5176.8195934.1284193.3.65f76a7di5WyeP
    @Override
    protected String sendSms(String mobile, Map<String, String> params, String signature, String template) {
        // 获取 签名内容 和模板id
        SignatureEntity signatureEntity = signatureService.getByCode(signature);
        String code = templateService.getConfigCodeByCode(config.getId(), template);

        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(config.getDomain());
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", config.get("RegionId"));
        request.putBodyParameter("PhoneNumbers", mobile);
        request.putBodyParameter("SignName", signatureEntity.getContent());
        request.putBodyParameter("TemplateCode", code);
        request.putBodyParameter("TemplateParam", JSON.toJSONString(params));

        try {
            CommonResponse response = client.getCommonResponse(request);
            JSONObject jsonObject = JSON.parseObject(response.getData());
            if (jsonObject.containsKey("Code") && jsonObject.getString("Code").equals("OK")) {
                return response.getData();
            } else {
                return failResponse(jsonObject.getString("Message"), response.getData());
            }
        } catch (Exception e) {
            log.error("Aliyun 短信发送失败: {} ,{}", mobile, template, e);
            return failResponse(e.getMessage(), e.getMessage());
        }
        //"{\"Message\":\"OK\",\"RequestId\":\"" + UUID.randomUUID().toString().toUpperCase() + "-@\",\"BizId\":\"" + System.currentTimeMillis() + "\",\"Code\":\"OK\"}";
    }

    @Override
    protected String sendSmsBatch(String[] mobiles, LinkedHashMap<String, String>[] params, String[] signatures, String[] templates) {
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(config.getDomain());
        request.setSysVersion(DateUtils.formatAsDate(LocalDateTime.now()));
        request.setSysAction("SendBatchSms");
        request.putQueryParameter("RegionId", config.get("RegionId"));

        if (mobiles.length <= 100) {
            request.putBodyParameter("PhoneNumbers", mobiles);
            request.putBodyParameter("SignName", signatures);
            request.putBodyParameter("TemplateCode", templates);
            request.putBodyParameter("TemplateParam", JSON.toJSONString(params));
            try {
                CommonResponse response = client.getCommonResponse(request);
                log.info(response.getData());
                return response.getData();
            } catch (ServerException e) {
                log.error("短信发送失败: {} ,{}", mobiles, templates, e);
            } catch (ClientException e) {
                log.error("短信发送失败: {} ,{}", mobiles, templates, e);
            }
        } else {
            int batchCount = (mobiles.length / 100) + 1;
            for (int i = 0; i < batchCount; i++) {
                String[] newMobiles = Arrays.copyOfRange(mobiles, i * 100, (i + 1) * 100);
                String[] newSignNames = Arrays.copyOfRange(signatures, i * 100, (i + 1) * 100);
                String[] newTemplates = Arrays.copyOfRange(templates, i * 100, (i + 1) * 100);
                LinkedHashMap[] newParams = Arrays.copyOfRange(params, i * 100, (i + 1) * 100);
                request.putBodyParameter("PhoneNumbers", newMobiles);
                request.putBodyParameter("SignName", newSignNames);
                request.putBodyParameter("TemplateCode", newTemplates);
                request.putBodyParameter("TemplateParam", JSON.toJSONString(newParams));
                try {
                    CommonResponse response = client.getCommonResponse(request);
                    log.info(response.getData());
                } catch (ServerException e) {
                    log.error("短信发送失败: {} ,{}", mobiles, templates, e);
                } catch (ClientException e) {
                    log.error("短信发送失败: {} ,{}", mobiles, templates, e);
                }
            }
        }
        return null;
    }
}
