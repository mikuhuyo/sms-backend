package com.sms.dto;

import com.sms.entity.ConfigEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 配置表
 *
 * @author yuelimin
 */
@Data
@ApiModel(description = "配置表")
public class ConfigDTO extends ConfigEntity {

    private int lastSuccessNumInAnHour;

    private int lastSuccessNum;

    @ApiModelProperty("签名信息")
    private List<SignatureDTO> signatureDTOS;
    @ApiModelProperty("签名id集合")
    private List<String> signatureIds;

    @ApiModelProperty("模板信息")
    private List<TemplateDTO> templateDTOS;
    @ApiModelProperty("模板id集合")
    private List<String> templateIds;

    @ApiModelProperty("测试")
    private PlatformDTO platformDTO;
}
