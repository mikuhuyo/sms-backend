package com.sms.manage.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pd.core.base.R;
import com.sms.entity.BlackListEntity;
import com.sms.manage.excel.BlackListExcelDTO;
import com.sms.manage.service.BlackListService;
import com.sms.mapper.BlackListMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 黑名单
 */
@Service
@Slf4j
public class BlackListServiceImpl extends ServiceImpl<BlackListMapper, BlackListEntity> implements BlackListService {

    private static Pattern PHONE_PATTERN = Pattern.compile("^[1]\\d{10}$");

    @SneakyThrows
    @Override
    public R upload(MultipartFile file) {
        ImportParams importParams = new ImportParams();
        List<BlackListExcelDTO> blackListExcelDTOs = ExcelImportUtil.importExcel(file.getInputStream(), BlackListExcelDTO.class, importParams);

        int total = blackListExcelDTOs.size();
        log.debug("黑名单导入 解析文件:{} 条", total);
        List<String> nullErrorMsg = new ArrayList<>();
        List<String> matchErrorMsg = new ArrayList<>();
        List<String> duplicateErrorMsg = new ArrayList<>();

        for (int i = 0; i < blackListExcelDTOs.size(); i++) {
            BlackListExcelDTO item = blackListExcelDTOs.get(i);
            if (StringUtils.isBlank(item.getMobile())) {
                nullErrorMsg.add("第" + (i + 1) + "条");
                continue;
            }
            if (!PHONE_PATTERN.matcher(item.getMobile()).matches()) {
                matchErrorMsg.add("第" + (i + 1) + "条");
                continue;
            }
            try {
                int flag = super.baseMapper.insert(BlackListEntity.builder().type("1").content(item.getMobile()).remark(item.getRemark()).build());
                if (flag <= 0) {
                    duplicateErrorMsg.add("第" + (i + 1) + "条");
                }
            } catch (Exception e) {
                if (e.getClass().getName().equals("org.springframework.dao.DuplicateKeyException")) {
                    duplicateErrorMsg.add("第" + (i + 1) + "条");
                } else {
                    log.warn("入库异常 {} : ", e.getClass().getName(), e);
                }
            }
        }


        int fail = nullErrorMsg.size() + matchErrorMsg.size() + duplicateErrorMsg.size();
        log.debug("黑名单导入 入库成功:{} 条", (total - fail));
        Map result = new HashMap<>();
        result.put("total", total);
        result.put("success", total - fail);
        result.put("fail", fail);
        result.put("nullErrorMsg", nullErrorMsg);
        result.put("matchErrorMsg", matchErrorMsg);
        result.put("duplicateErrorMsg", duplicateErrorMsg);
        return R.success(result);
    }
}
