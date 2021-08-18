package com.sms.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseParamsDTO implements Serializable {
    @ApiModelProperty("接入key")
    private String accessKeyId;
    @ApiModelProperty("认证值")
    private String encryption;
    @ApiModelProperty("发送时间戳")
    private String timestamp;
    @ApiModelProperty("定时时间 yyyy-MM-dd HH:mm")
    private String sendTime;
}
