package com.sms.sdk.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SmsParamsDTO extends BaseParamsDTO {
    //手机号
    private String mobile;
    //模板编码
    private String template;
    //签名编码
    private String signature;
    //参数
    private Map<String, String> params;

}
