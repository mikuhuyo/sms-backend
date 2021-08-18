package com.sms.manage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pd.core.base.BaseController;
import com.pd.core.base.R;
import com.pd.core.utils.DateUtils;
import com.sms.entity.SendLogEntity;
import com.sms.manage.service.ReceiveLogService;
import com.sms.manage.service.SendLogService;
import com.sms.vo.ReceiveLogVO;
import com.sms.vo.SendLogPageVO;
import com.sms.vo.SendLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 日志表
 */
@RestController
@RequestMapping("log")
@Api(tags = "日志")
public class LogController extends BaseController {
    @Autowired
    private SendLogService sendLogService;
    @Autowired
    private ReceiveLogService receiveLogService;

    @GetMapping("sendPage")
    @ApiOperation("发送日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页码, 从1开始", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页显示记录数", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "排序方式", value = "排序方式, 可选值(asc.desc)", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String")
    })
    public R<Page<SendLogVO>> sendPage(SendLogVO sendLogVO) {
        Page<SendLogVO> page = getPage();
        Map<String, Object> params = new HashMap<>();
        if (getStartCreateTime() != null) {
            params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        if (getEndCreateTime() != null) {
            params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        params.put("signatureName", sendLogVO.getSignatureName());
        params.put("templateName", sendLogVO.getTemplateName());
        params.put("mobile", sendLogVO.getMobile());
        params.put("platformName", sendLogVO.getPlatformName());
        Page<SendLogVO> sendLogVOPage = sendLogService.pageLog(page, params);
        return R.success(sendLogVOPage);
    }

    @GetMapping("receivePage")
    @ApiOperation("接收日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页码, 从1开始", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页显示记录数", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "排序方式", value = "排序方式, 可选值(asc.desc)", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String")
    })
    public R<Page<ReceiveLogVO>> receivePage(ReceiveLogVO receiveLogVO) {
        Page<ReceiveLogVO> page = getPage();
        Map<String, Object> params = new HashMap<>();
        if (getStartCreateTime() != null) {
            params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }

        if (getEndCreateTime() != null) {
            params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        params.put("platformName", receiveLogVO.getPlatformName());
        params.put("signatureName", receiveLogVO.getSignatureName());
        params.put("templateName", receiveLogVO.getTemplateName());
        Page<ReceiveLogVO> receiveLogVOPage = receiveLogService.pageLog(page, params);
        return R.success(receiveLogVOPage);
    }

    @GetMapping("{id}")
    @ApiOperation("信息")
    public R<SendLogEntity> get(@PathVariable("id") Long id) {
        SendLogEntity data = sendLogService.getById(id);

        return R.success(data);
    }


    @GetMapping("sendLogPage")
    @ApiOperation("发送记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页码, 从1开始", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页显示记录数", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "排序方式", value = "排序方式, 可选值(asc.desc)", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String")
    })
    public R<Page<SendLogPageVO>> sendLogPage(SendLogPageVO sendLogPageVO) {
        Page<SendLogPageVO> page = getPage();
        Page<SendLogPageVO> sendLogVOPage = sendLogService.sendLogPage(page, sendLogPageVO);
        List<SendLogPageVO> record = sendLogVOPage.getRecords().stream().map(item -> {
            if (StringUtils.isNotBlank(item.getTemplateContent())) {
                String content = item.getTemplateContent().replaceAll("(\\$\\{)([\\w]+)(\\})", "******");
                item.setContentText(content);
                item.buildRemark();
                item.cleanBigField();
            }
            return item;
        }).collect(Collectors.toList());
        page.setRecords(record);
        return R.success(page);
    }

}
