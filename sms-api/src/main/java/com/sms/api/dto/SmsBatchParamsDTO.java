package com.sms.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@ApiModel(description = "短信批量发送参数")
public class SmsBatchParamsDTO extends BaseParamsDTO {
    @ApiModelProperty("手机号集合")
    private List<String> mobile = new ArrayList<>();
    @ApiModelProperty("模板编码集合")
    private List<String> template = new ArrayList<>();
    @ApiModelProperty("签名编码集合")
    private List<String> signature = new ArrayList<>();
    @ApiModelProperty("参数集合")
    private List<LinkedHashMap<String, String>> params = new ArrayList<>();
    @ApiModelProperty("批次编码")
    private String batchCode;

}
