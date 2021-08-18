package com.sms.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "发送记录")
public class SendLogPageVO {
    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("签名名称")
    private String signatureName;

    @ApiModelProperty("模板名称")
    private String templateName;

    @ApiModelProperty("模板类型")
    private String templateType;

    @ApiModelProperty("模板内容")
    private String templateContent;

    @ApiModelProperty("发信方")
    private String platformName;

    @ApiModelProperty(value = "短信内容")
    private String contentText;

    @ApiModelProperty(value = "状态: 0失败, 1成功")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "时间 精确到日")
    private String date;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "营销短信主键")
    private String marketingId;

    private String response;

    private String error;

    private String configPlatform;

    public void buildRemark() {
        if (status == 1) {
            return;
        }
        switch (configPlatform) {
            case "未找到":
                remark = "未找到合适配置";
                break;
            case "MengWang":

            case "Aliyun":
                //{"Message":"OK","RequestId":"3D412865-0B97-49FD-B8D7-AE374B543FF8","BizId":"867220103509287440^0","Code":"OK"}
            case "Feige":
                //{"SendId":"","InvalidCount":0,"SuccessCount":0,"BlackCount":0,"Code":10008,"Message":"模板Id不能为空"}
                if (StringUtils.isNotEmpty(response)) {
                    JSONObject jsonObject = JSON.parseObject(response);
                    if (jsonObject.containsKey("Message")) {
                        remark = jsonObject.getString("Message");
                    }
                } else {
                    remark = error;
                }
                break;
            case "Huawei":
                if (StringUtils.isNotEmpty(response)) {
                    JSONObject jsonObject = JSON.parseObject(response);
                    if (jsonObject.containsKey("description")) {
                        remark = jsonObject.getString("description");
                    }
                } else {
                    remark = error;
                }
                break;
            case "Jd":
                if (StringUtils.isNotEmpty(response)) {
                    JSONObject jsonObject = JSON.parseObject(response);
                    if (jsonObject.containsKey("result")) {
                        remark = jsonObject.getJSONObject("result").getString("message");
                    }
                } else {
                    remark = error;
                }
                break;
            case "Lexin":
                if (StringUtils.isNotEmpty(response)) {
                    JSONObject jsonObject = JSON.parseObject(response);
                    if (jsonObject.containsKey("replyMsg")) {
                        remark = jsonObject.getString("replyMsg");
                    }
                } else {
                    remark = error;
                }
                break;
            default:
                remark = error;
                break;
        }
    }

    public void cleanBigField() {
        response = null;
        error = null;
    }
}
