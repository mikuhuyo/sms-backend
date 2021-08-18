package com.sms.dto;

import com.sms.entity.TimingPushEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 定时发送
 *
 * @author yuelimin
 */
@Data
@ApiModel(description = "定时发送")
public class TimingPushDTO extends TimingPushEntity {

}
