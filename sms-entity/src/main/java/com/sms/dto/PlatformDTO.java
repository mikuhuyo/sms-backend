package com.sms.dto;

import com.sms.entity.PlatformEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 配置表
 *
 * @author yuelimin
 */
@Data
@ApiModel(description = "接入平台")
public class PlatformDTO extends PlatformEntity {

}
