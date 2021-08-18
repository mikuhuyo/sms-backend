/**
 * Copyright (c) 2019 联智合创 All rights reserved.
 * <p>
 * http://www.witlinked.com
 * <p>
 * 版权所有, 侵权必究！
 */

package com.sms.server.sms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sms.entity.TemplateEntity;
import com.sms.server.entity.SmsConfig;
import com.sms.utils.StringHelper;
import io.undertow.util.StatusCodes;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 梦网
 * http://con.monyun.cn:9960/frame/#console/console.html
 */
@Slf4j
public class MengWangSmsService extends AbstractSmsService {

    private static final Map<Integer, String> FAIL_CODE = new HashMap() {{
        put(-100001, "鉴权不通过,请检查账号,密码,时间戳,固定串,以及MD5算法是否按照文档要求进行设置");
        put(-100002, "用户多次鉴权不通过,请检查帐号,密码,时间戳,固定串,以及MD5算法是否按照文档要求进行设置");
        put(-100003, "用户欠费");
        put(-100004, "custid或者exdata字段填写不合法");
        put(-100011, "短信内容超长");
        put(-100012, "手机号码不合法");
        put(-100014, "手机号码超过最大支持数量（1000）");
        put(-100029, "端口绑定失败");
        put(-100056, "用户账号登录的连接数超限");
        put(-100057, "用户账号登录的IP错误");
        put(-100999, "平台数据库内部错误");
    }};

    public MengWangSmsService(SmsConfig config) {
        this.config = config;
    }

    @SneakyThrows
    @Override
    public String sendSms(String mobile, Map<String, String> params, String signature, String template) {
        // 获取 签名内容 和模板id
        // SignatureEntity signatureEntity = signatureService.getByCode(signature);
        TemplateEntity templateEntity = templateService.getByCode(template);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HashMap hashMap = new HashMap();
        hashMap.put("apikey", config.getAccessKeyId());
        hashMap.put("mobile", mobile);
        hashMap.put("content", URLEncoder.encode(StringHelper.renderString(templateEntity.getContent(), params), "GBK"));
        //您的验证码是668866, 在5分钟内输入有效.如非本人操作请忽略此短信.

        HttpPost post = new HttpPost(config.getDomain() + config.get("single_send"));

        post.setHeader("Content-Type", "application/json; charset=UTF-8");

        StringEntity stringEntity = new StringEntity(JSON.toJSONString(hashMap), "UTF-8");
        post.setEntity(stringEntity);

        CloseableHttpResponse response = httpclient.execute(post);

        try {
            HttpEntity entity = response.getEntity();

            if (StatusCodes.OK == response.getStatusLine().getStatusCode()) {
                log.info("httpRequest access success, StatusCode is:{}", response.getStatusLine()
                        .getStatusCode());
                String responseContent = EntityUtils.toString(entity);
                JSONObject jsonObject = JSON.parseObject(responseContent);
                if (jsonObject.containsKey("result") && jsonObject.getInteger("result") == 0) {
                    return responseContent;
                } else {
                    jsonObject.put("Message", FAIL_CODE.get(jsonObject.getInteger("result")));
                    return failResponse(jsonObject.getString("Message"), responseContent);
                }
            } else {
                log.error("httpRequest access fail ,StatusCode is:{}", response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.error("error :", e);
        } finally {
            post.releaseConnection();
        }
        return null;
    }

    @Override
    public String sendSmsBatch(String[] mobiles, LinkedHashMap<String, String>[] params, String[] signNames, String[]
            templates) {

        return null;
    }
}
