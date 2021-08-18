package com.sms.dto;

import com.sms.entity.BlackListEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 黑名单
 *
 * @author yuelimin
 */
@Data
@ApiModel(description = "黑名单")
public class BlackListDTO extends BlackListEntity {

}
