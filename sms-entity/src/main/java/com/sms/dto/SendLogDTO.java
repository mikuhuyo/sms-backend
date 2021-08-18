package com.sms.dto;

import com.sms.entity.SendLogEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 日志表
 *
 * @author yuelimin
 */
@Data
@ApiModel(description = "日志表")
public class SendLogDTO extends SendLogEntity {

}
