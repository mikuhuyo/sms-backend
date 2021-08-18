package com.sms.sdk.service.impl;

import com.alibaba.fastjson.JSON;
import com.sms.sdk.dto.BaseParamsDTO;
import com.sms.sdk.dto.R;
import com.sms.sdk.dto.SmsBatchParamsDTO;
import com.sms.sdk.dto.SmsParamsDTO;
import com.sms.sdk.service.SmsSendService;
import com.sms.sdk.utils.SmsEncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsSendServiceImpl implements SmsSendService {
    @Value("${sms-key.sms.auth}")
    private boolean auth;
    @Value("${sms-key.sms.domain}")
    private String domain;
    @Value("${sms-key.sms.accessKeyId}")
    private String accessKeyId;
    @Value("${sms-key.sms.accessKeySecret}")
    private String accessKeySecret;

    private String send = "/sms/send";
    private String batchSend = "/sms/batchSend";

    /**
     * 单条短信
     *
     * @param smsParamsDTO
     * @return
     */
    @Override
    public R sendSms(SmsParamsDTO smsParamsDTO) {
        String url = domain + send;
        return send(smsParamsDTO, url);
    }

    /**
     * 批量短信
     *
     * @param smsBatchParamsDTO
     * @return
     */
    @Override
    public R batchSendSms(SmsBatchParamsDTO smsBatchParamsDTO) {
        String url = domain + batchSend;
        return send(smsBatchParamsDTO, url);
    }

    /**
     * 通过HttpClient发送post请求, 请求短信接收服务HTTP接口
     *
     * @param baseParamsDTO
     * @param url
     * @return
     */
    private R send(BaseParamsDTO baseParamsDTO, String url) {
        //设置accessKeyId
        baseParamsDTO.setAccessKeyId(accessKeyId);
        if (auth) {
            if (StringUtils.isBlank(accessKeyId) || StringUtils.isBlank(accessKeySecret)) {
                R.fail("accessKey 不能为空");
            }
            baseParamsDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
            baseParamsDTO.setEncryption(SmsEncryptionUtils.encode(baseParamsDTO.getEncryption(), baseParamsDTO.getAccessKeyId(), accessKeySecret));
        }

        if (StringUtils.isBlank(domain)) {
            R.fail("domain 不能为空");
        }
        //HTTP客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //Post请求对象
        HttpPost post = new HttpPost(url);
        //设置请求头
        post.setHeader("Content-Type", "application/json; charset=UTF-8");
        //构造请求体
        StringEntity stringEntity = new StringEntity(JSON.toJSONString(baseParamsDTO), "UTF-8");
        //设置请求体
        post.setEntity(stringEntity);

        try {
            //发送请求
            CloseableHttpResponse response = httpclient.execute(post);
            //获得响应信息
            HttpEntity entity = response.getEntity();
            //解析响应状态码
            if (200 == response.getStatusLine().getStatusCode()) {
                log.info("httpRequest access success, StatusCode is:{}", response.getStatusLine()
                        .getStatusCode());
                String responseContent = EntityUtils.toString(entity);
                log.info("responseContent is :" + responseContent);
                return JSON.parseObject(responseContent, R.class);
            } else {
                log.error("httpRequest access fail ,StatusCode is:{}", response.getStatusLine().getStatusCode());
                return R.fail("status is " + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.error("error :", e);
            return R.fail(e.getMessage());
        } finally {
            post.releaseConnection();
        }
    }
}
