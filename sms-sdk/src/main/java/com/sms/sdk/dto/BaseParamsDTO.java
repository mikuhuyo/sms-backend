package com.sms.sdk.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseParamsDTO implements Serializable {
    //接入key
    private String accessKeyId;
    //认证值
    private String encryption;
    //发送时间戳
    private String timestamp;
    //定时时间 yyyy-MM-dd HH:mm
    private String sendTime;
}
