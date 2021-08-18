package com.sms.vo;

import com.sms.entity.ReceiveLogEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "接收日志表")
public class ReceiveLogVO extends ReceiveLogEntity {

    @ApiModelProperty("通道名称")
    private String configName;
    @ApiModelProperty("签名名称")
    private String signatureName;
    @ApiModelProperty("模板名称")
    private String templateName;
}
