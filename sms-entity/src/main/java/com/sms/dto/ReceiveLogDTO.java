package com.sms.dto;

import com.sms.entity.ReceiveLogEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 接收日志表
 *
 * @author yuelimin
 */
@Data
@ApiModel(description = "接收日志表")
public class ReceiveLogDTO extends ReceiveLogEntity {

}
