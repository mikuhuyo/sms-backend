package com.sms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sms.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 黑名单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@TableName("black_list")
@ApiModel(description = "黑名单")
public class BlackListEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("类型: 1短信, 2邮件, 3微信")
    private String type;
    @ApiModelProperty("内容: 手机号")
    private String content;
    @ApiModelProperty("备注")
    private String remark;

}
