package com.sms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sms.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人工处理任务表
 */

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("manual_process")
@ApiModel("人工处理任务表")
public class ManualProcessEntity extends BaseEntity {

    @ApiModelProperty("模板")
    private String template;

    @ApiModelProperty("签名")
    private String signature;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("请求参数")
    private String request;

    @ApiModelProperty("通道id集合")
    private String configIds;

    @ApiModelProperty("状态 0新建, 1处理中, 2处理成功, 3处理失败")
    private int status;

    @ApiModelProperty("备注")
    private String remark;

}
