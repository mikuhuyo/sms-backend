package com.sms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sms.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("config")
@ApiModel(description = "配置表")
public class ConfigEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "平台")
    private String platform;

    @ApiModelProperty(value = "域名")
    private String domain;

    @ApiModelProperty(value = "秘钥id")
    private String accessKeyId;

    @ApiModelProperty(value = "秘钥值")
    private String accessKeySecret;

    @ApiModelProperty(value = "其他配置 json格式")
    private String other;

    @ApiModelProperty(value = "是否可用: 0不可用")
    private Integer isActive;

    @ApiModelProperty(value = "是否开启: 0未开启")
    private Integer isEnable;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "级别")
    private Integer level;

    @ApiModelProperty(value = "通道类型, 1: 文字, 2: 语音, 3: 推送")
    private Integer channelType;

}
