package com.sms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sms.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接收日志表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("receive_log")
@ApiModel("接收日志")
public class ReceiveLogEntity extends BaseEntity {

    @ApiModelProperty("接入平台id")
    private String platformId;

    @ApiModelProperty("接入平台名称")
    private String platformName;

    @ApiModelProperty("业务信息 由平台方自定义")
    private String business;

    @ApiModelProperty("配置主键集合")
    private String configIds;

    @ApiModelProperty("模板")
    private String template;

    @ApiModelProperty("签名")
    private String signature;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("请求参数")
    private String request;

    @ApiModelProperty("错误信息")
    private String error;

    @ApiModelProperty("耗时")
    private Long useTime;

    @ApiModelProperty(value = "日志id")
    private String apiLogId;

    @ApiModelProperty("状态: 0失败, 1成功")
    private int status;

    @ApiModelProperty("备注")
    private String remark;

}

