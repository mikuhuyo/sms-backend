package com.sms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("数量统计")
public class StatisticsCountVO {

    @ApiModelProperty("总数")
    private int count;
    @ApiModelProperty("成功数")
    private int success;
    @ApiModelProperty("失败数")
    private int fail;
    @ApiModelProperty("时间")
    private String date;
    @ApiModelProperty("成功率")
    private String successRate;
}
