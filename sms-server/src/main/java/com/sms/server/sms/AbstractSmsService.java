package com.sms.server.sms;

import com.sms.server.entity.SmsConfig;
import com.sms.server.service.SignatureService;
import com.sms.server.service.TemplateService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信发送的抽象类
 * 1.发送单条短信
 * 2.发送批量短信
 * 不同渠道的服务都需要继承此抽象类
 *
 * @author
 */
public abstract class AbstractSmsService {
    /**
     * 通道配置信息
     */
    protected SmsConfig config;

    /**
     * 签名
     */
    protected SignatureService signatureService;

    /**
     * 模板
     */
    protected TemplateService templateService;

    public SmsConfig getConfig() {
        return config;
    }

    /**
     * 发送短信
     *
     * @param mobile    手机号
     * @param params    参数
     * @param signature 短信签名
     * @param template  短信模板
     * @return
     */
    public String send(String mobile, Map<String, String> params, String signature, String template) {
        return this.sendSms(mobile, params, signature, template);
    }

    /**
     * 批量发送短信
     *
     * @param mobiles    手机号
     * @param params     参数
     * @param signatures 短信签名
     * @param templates  短信模板
     * @return
     */
    public String sendBatch(String[] mobiles, LinkedHashMap<String, String>[] params, String[] signatures, String[] templates) {
        return this.sendSmsBatch(mobiles, params, signatures, templates);
    }

    /**
     * 发送短信
     *
     * @param mobile    手机号
     * @param params    参数
     * @param signature 短信签名
     * @param template  短信模板
     * @return
     */
    protected abstract String sendSms(String mobile, Map<String, String> params, String signature, String template);


    /**
     * 批量发送短信
     *
     * @param mobiles    手机号
     * @param params     参数
     * @param signatures 短信签名
     * @param templates  短信模板
     * @return
     */
    protected abstract String sendSmsBatch(String[] mobiles, LinkedHashMap<String, String>[] params, String[] signatures, String[] templates);

    protected String failResponse(String msg, String response) {
        return "FAIL@#@" + msg + "@#@" + response;
    }
}
