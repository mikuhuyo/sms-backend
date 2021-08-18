package com.sms.dto;

import com.sms.entity.ManualProcessEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 人工处理任务表
 *
 * @author yuelimin
 */
@Data
@ApiModel(description = "人工处理任务表")
public class ManualProcessDTO extends ManualProcessEntity {

}
