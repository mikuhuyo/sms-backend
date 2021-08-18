package com.sms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sms.entity.base.BaseEntity;
import com.sms.exception.SmsException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日志表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("send_log")
@ApiModel(description = "发送日志")
public class SendLogEntity extends BaseEntity {

    @ApiModelProperty(value = "配置主键")
    private String configId;

    @ApiModelProperty(value = "配置平台")
    private String configPlatform;

    @ApiModelProperty(value = "配置名称")
    private String configName;

    @ApiModelProperty(value = "模板")
    private String template;

    @ApiModelProperty(value = "签名")
    private String signature;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "请求参数")
    private String request;

    @ApiModelProperty(value = "返回参数")
    private String response;

    @ApiModelProperty(value = "错误信息")
    private String error;

    @ApiModelProperty(value = "耗时")
    private Long useTime;

    @ApiModelProperty(value = "状态: 0失败, 1成功")
    private Integer status;

    @ApiModelProperty(value = "日志id")
    private String apiLogId;

    @ApiModelProperty(value = "备注")
    private String remark;

//    public void buildRemark() {
//		this.remark = error;
//    }

    public void checkResponse(String response) {
        if (response.startsWith("FAIL@#@")) {
            String[] responseArray = response.split("@#@");
            this.response = responseArray[2];
            throw new SmsException(responseArray[1]);
        } else {
            this.response = response;
        }
    }
}
