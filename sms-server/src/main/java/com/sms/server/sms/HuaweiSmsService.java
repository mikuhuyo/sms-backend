package com.sms.server.sms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sms.entity.SignatureEntity;
import com.sms.entity.TemplateEntity;
import com.sms.server.entity.SmsConfig;
import com.sms.utils.StringHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

//如果JDK版本是1.8,可使用原生Base64类

/**
 * 华为
 * 控制台 https://console.huaweicloud.com/message/?region=cn-north-4#/msgSms/overview
 * API: https://support.huaweicloud.com/devg-msgsms/sms_04_0002.html
 */
@Slf4j
public class HuaweiSmsService extends AbstractSmsService {

    //无需修改,用于格式化鉴权头域,给"X-WSSE"参数赋值
    private static final String WSSE_HEADER_FORMAT = "UsernameToken Username=\"%s\",PasswordDigest=\"%s\",Nonce=\"%s\",Created=\"%s\"";
    //无需修改,用于格式化鉴权头域,给"Authorization"参数赋值
    private static final String AUTH_HEADER_VALUE = "WSSE realm=\"SDP\",profile=\"UsernameToken\",type=\"Appkey\"";

    public HuaweiSmsService(SmsConfig config) {
        this.config = config;
    }

    /**
     * 构造请求Body体
     *
     * @param sender
     * @param receiver
     * @param templateId
     * @param templateParas
     * @param statusCallbackUrl
     * @param signature         | 签名名称,使用国内短信通用模板时填写
     * @return
     */
    static String buildRequestBody(String sender, String receiver, String templateId, String templateParas,
                                   String statusCallbackUrl, String signature) {
        if (null == sender || null == receiver || null == templateId || sender.isEmpty() || receiver.isEmpty()
                || templateId.isEmpty()) {
            System.out.println("buildRequestBody(): sender, receiver or templateId is null.");
            return null;
        }
        List<NameValuePair> keyValues = new ArrayList<NameValuePair>();

        keyValues.add(new BasicNameValuePair("from", sender));
        keyValues.add(new BasicNameValuePair("to", receiver));
        keyValues.add(new BasicNameValuePair("templateId", templateId));
        if (null != templateParas && !templateParas.isEmpty()) {
            keyValues.add(new BasicNameValuePair("templateParas", templateParas));
        }
        if (null != statusCallbackUrl && !statusCallbackUrl.isEmpty()) {
            keyValues.add(new BasicNameValuePair("statusCallback", statusCallbackUrl));
        }
        if (null != signature && !signature.isEmpty()) {
            keyValues.add(new BasicNameValuePair("signature", signature));
        }

        return URLEncodedUtils.format(keyValues, Charset.forName("UTF-8"));
    }

    @SneakyThrows
    @Override
    public String sendSms(String mobile, Map<String, String> params, String signature, String template) {
        SignatureEntity signatureEntity = signatureService.getByCode(signature);
        TemplateEntity templateEntity = templateService.getByCode(template);
        String signatureCode = signatureService.getConfigCodeByCode(config.getId(), signature);
        String templateCode = templateService.getConfigCodeByCode(config.getId(), template);

        //必填,请参考"开发准备"获取如下数据,替换为实际值
        String url = config.getDomain() + "/sms/batchSendSms/v1"; //"https://rtcsms.cn-north-1.myhuaweicloud.com:10743/sms/batchSendSms/v1"; //APP接入地址+接口访问URI
        String appKey = config.getAccessKeyId(); //"nhI5IxULe4vcgMTP6h29220gEtg6"; //APP_Key
        String appSecret = config.getAccessKeySecret(); //"q4Ii87BhST9vcs8wvrzN80SfD7Al"; //APP_Secret
        String sender = signatureCode; //"8820102933300"; //国内短信签名通道号或国际/港澳台短信通道号
        String templateId = templateCode;//"3e9f0a8978d44578aba218ecf56db4a2"; //模板ID

        //条件必填,国内短信关注,当templateId指定的模板类型为通用模板时生效且必填,必须是已审核通过的,与模板类型一致的签名名称
        //国际/港澳台短信不用关注该参数
        signature = signatureEntity.getContent(); //签名名称

        //必填,全局号码格式(包含国家码),示例:+8615123456789,多个号码之间用英文逗号分隔
        String receiver = mobile; //短信接收人号码

        //选填,短信状态报告接收地址,推荐使用域名,为空或者不填表示不接收状态报告
        String statusCallBack = "";

        /**
         * 选填,使用无变量模板时请赋空值 String templateParas = "";
         * 单变量模板示例:模板内容为"您的验证码是${1}"时,templateParas可填写为"[\"369751\"]"
         * 双变量模板示例:模板内容为"您有${1}件快递请到${2}领取"时,templateParas可填写为"[\"3\",\"人民公园正门\"]"
         * 模板中的每个变量都必须赋值, 且取值不能为空
         * 查看更多模板和变量规范:产品介绍>模板和变量规范
         */
        List<String> paramsField = StringHelper.getSubUtil(templateEntity.getContent());
        StringBuffer templateParasBuff = new StringBuffer();
        if (!CollectionUtils.isEmpty(paramsField)) {
            templateParasBuff.append("[");
            for (String field : paramsField) {
                templateParasBuff.append("\"").append(params.get(field)).append("\"").append(",");
            }
            templateParasBuff.deleteCharAt(templateParasBuff.length() - 1);
            templateParasBuff.append("]");
        }

        String templateParas = templateParasBuff.toString(); //模板变量, 此处以单变量验证码短信为例, 请客户自行生成6位验证码, 并定义为字符串类型, 以杜绝首位0丢失的问题（例如: 002569变成了2569）.

        //请求Body,不携带签名名称时,signature请填null
        String body = buildRequestBody(sender, receiver, templateId, templateParas, statusCallBack, signature);
        if (null == body || body.isEmpty()) {
            System.out.println("body is null.");
            return failResponse("消息体为空", "");
        }

        //请求Headers中的X-WSSE参数值
        String wsseHeader = buildWsseHeader(appKey, appSecret);
        if (null == wsseHeader || wsseHeader.isEmpty()) {
            return failResponse("WSSE 消息头为空", "");
        }

        //为防止因HTTPS证书认证失败造成API调用失败,需要先忽略证书信任问题
        CloseableHttpClient client = HttpClients.custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null,
                        (x509CertChain, authType) -> true).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

        HttpResponse response = client.execute(RequestBuilder.create("POST")//请求方法POST
                .setUri(url)
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .addHeader(HttpHeaders.AUTHORIZATION, AUTH_HEADER_VALUE)
                .addHeader("X-WSSE", wsseHeader)
                .setEntity(new StringEntity(body)).build());

        log.info("httpRequest access success, header is:{}", response.toString()); //打印响应头域信息

        String responseContent = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = JSON.parseObject(responseContent);
        if (jsonObject.containsKey("code") && jsonObject.getString("code").equals("000000")) {
            return responseContent;
        } else {
            return failResponse(jsonObject.getString("description"), responseContent);
        }
    }

    @Override
    public String sendSmsBatch(String[] mobiles, LinkedHashMap<String, String>[] params, String[] signNames, String[]
            templates) {

        return null;
    }

    /**
     * 构造X-WSSE参数值
     *
     * @param appKey
     * @param appSecret
     * @return
     */
    private String buildWsseHeader(String appKey, String appSecret) {
        if (null == appKey || null == appSecret || appKey.isEmpty() || appSecret.isEmpty()) {
            System.out.println("buildWsseHeader(): appKey or appSecret is null.");
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String time = sdf.format(new Date()); //Created
        String nonce = UUID.randomUUID().toString().replace("-", ""); //Nonce

        byte[] passwordDigest = DigestUtils.sha256(nonce + time + appSecret);
        String hexDigest = Hex.encodeHexString(passwordDigest);

        //如果JDK版本是1.8,请加载原生Base64类,并使用如下代码
        String passwordDigestBase64Str = Base64.getEncoder().encodeToString(hexDigest.getBytes()); //PasswordDigest
        //如果JDK版本低于1.8,请加载三方库提供Base64类,并使用如下代码
        //String passwordDigestBase64Str = Base64.encodeBase64String(hexDigest.getBytes(Charset.forName("utf-8"))); //PasswordDigest
        //若passwordDigestBase64Str中包含换行符,请执行如下代码进行修正
        //passwordDigestBase64Str = passwordDigestBase64Str.replaceAll("[\\s*\t\n\r]", "");

        return String.format(WSSE_HEADER_FORMAT, appKey, passwordDigestBase64Str, nonce, time);
    }

}
