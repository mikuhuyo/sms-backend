package com.sms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sms.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模板表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("template")
@ApiModel(description = "模板表")
public class TemplateEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "模板名称")
    private String name;

    @ApiModelProperty(value = "模板编码")
    private String code;

    @ApiModelProperty(value = "模板内容")
    private String content;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "模板类型 1:验证码 2:通知 3:推广")
    private int type;

}
