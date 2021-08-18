package com.sms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 配置表
 *
 * @author yuelimin
 */
@Data
@ApiModel(description = "通道修改关联中的其他配置")
public class ConfigUpdateOtherDTO {

    @ApiModelProperty("签名id")
    private String signatureId;
    @ApiModelProperty("三方通道签名")
    private String configSignatureCode;

    @ApiModelProperty("模板id")
    private String templateId;
    @ApiModelProperty("三方通道模板")
    private String configTemplateCode;

    @ApiModelProperty("通道id")
    private String configId;
}
