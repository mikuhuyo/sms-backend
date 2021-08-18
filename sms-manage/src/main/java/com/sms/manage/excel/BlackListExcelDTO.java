package com.sms.manage.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlackListExcelDTO implements Serializable {

    @Excel(name = "手机号", orderNum = "1")
    private String mobile;

    @Excel(name = "备注", orderNum = "2")
    private String remark;

}
