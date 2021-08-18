package com.sms.sdk.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Data
public class SmsBatchParamsDTO extends BaseParamsDTO {
    //手机号集合
    private List<String> mobile = new ArrayList<>();
    //模板编码集合
    private List<String> template = new ArrayList<>();
    //签名编码集合
    private List<String> signature = new ArrayList<>();
    //参数集合
    private List<LinkedHashMap<String, String>> params = new ArrayList<>();
    //批次编码
    private String batchCode;

    public SmsBatchParamsDTO addMobile(String mobile) {
        this.mobile.add(mobile);
        return this;
    }

    public SmsBatchParamsDTO addTemplate(String template) {
        this.template.add(template);
        return this;
    }

    public SmsBatchParamsDTO addSignature(String signature) {
        this.signature.add(signature);
        return this;
    }

    public SmsBatchParamsDTO addParams(LinkedHashMap<String, String> params) {
        this.params.add(params);
        return this;
    }
}
