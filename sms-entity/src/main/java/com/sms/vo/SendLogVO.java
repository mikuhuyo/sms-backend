package com.sms.vo;

import com.sms.entity.SendLogEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 日志表
 *
 * @author yuelimin
 */
@Data
@ApiModel(description = "发送日志")
public class SendLogVO extends SendLogEntity {
    @ApiModelProperty("签名名称")
    private String signatureName;
    @ApiModelProperty("模板名称")
    private String templateName;

    @ApiModelProperty("接入平台id")
    private String platformId;
    @ApiModelProperty("接入平台名称")
    private String platformName;
}
