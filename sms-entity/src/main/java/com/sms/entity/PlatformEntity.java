package com.sms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sms.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接入平台
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("platform")
@ApiModel(description = "接入平台")
public class PlatformEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("认证 key")
    private String accessKeyId;

    @ApiModelProperty("认证 秘钥")
    private String accessKeySecret;

    @ApiModelProperty("ip地址")
    private String ipAddr;

    @ApiModelProperty("是否需要鉴权 0 不需要")
    private Integer needAuth;

    @ApiModelProperty("是否可用: 0不可用")
    private Integer isActive;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("等级")
    private Integer level;

}
