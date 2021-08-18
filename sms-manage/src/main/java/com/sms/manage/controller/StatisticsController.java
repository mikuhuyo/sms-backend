package com.sms.manage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pd.core.base.BaseController;
import com.pd.core.base.R;
import com.pd.core.utils.DateUtils;
import com.sms.entity.ReceiveLogEntity;
import com.sms.entity.SendLogEntity;
import com.sms.entity.base.BaseEntity;
import com.sms.manage.service.ReceiveLogService;
import com.sms.manage.service.SendLogService;
import com.sms.vo.SendLogVO;
import com.sms.vo.StatisticsCountVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 统计
 */
@RestController
@RequestMapping("statistics")
@Api(tags = "统计")
public class StatisticsController extends BaseController {
    @Autowired
    private SendLogService sendLogService;
    @Autowired
    private ReceiveLogService receiveLogService;

    /**
     * 获取两个日期之间的所有日期
     *
     * @param startTime 开始日期
     * @param endTime   结束日期
     * @return
     */
    public static List<String> getDays(LocalDateTime startTime, LocalDateTime endTime, String format) {

        // 返回的日期集合
        List<String> days = new ArrayList<>();

        DateFormat dateFormat = new SimpleDateFormat(format);

        Date start = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant());

        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(start);

        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(end);
        // 日期加1(包含结束)
        // tempEnd.add(Calendar.DATE, +1);
        while (tempStart.before(tempEnd)) {
            days.add(dateFormat.format(tempStart.getTime()));
            tempStart.add(Calendar.DAY_OF_YEAR, 1);
        }

        return days;
    }

    @GetMapping("count/page")
    @ApiOperation("发送量统计(列表)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页码, 从1开始", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页显示记录数", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String")
    })
    public R<Page<StatisticsCountVO>> countPage(SendLogVO sendLogVO) {
        Page<StatisticsCountVO> page = getPage();
        Map<String, Object> params = new HashMap<>();
        if (getStartCreateTime() != null) {
            params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        if (getEndCreateTime() != null) {
            params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        params.put("signatureName", sendLogVO.getSignatureName());
        params.put("templateName", sendLogVO.getTemplateName());
        Page<StatisticsCountVO> statisticsCountVOPage = sendLogService.countPage(page, params);

        return R.success(statisticsCountVOPage);
    }

    @GetMapping("count")
    @ApiOperation("发送量统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间", paramType = "query", required = true, dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间", paramType = "query", required = true, dataType = "String")
    })
    public R<StatisticsCountVO> count() {
        LocalDateTime start = getStartCreateTime();
        LocalDateTime end = getEndCreateTime();

        LambdaQueryWrapper<SendLogEntity> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.between(BaseEntity::getCreateTime, start, end);
        int count = sendLogService.count(countWrapper);

        // 成功
        countWrapper.eq(SendLogEntity::getStatus, 1);
        int success = sendLogService.count(countWrapper);

        return R.success(StatisticsCountVO.builder().count(count).success(success).fail(count - success).build());
    }

    @GetMapping("trend")
    @ApiOperation("发送量趋势")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间", paramType = "query", required = true, dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间", paramType = "query", required = true, dataType = "String")
    })
    public R trend() {
        Map params = new HashMap();
        if (getStartCreateTime() != null) {
            params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        if (getEndCreateTime() != null) {
            params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }

        List<StatisticsCountVO> logs = sendLogService.trend(params);
        Map<String, StatisticsCountVO> logsMap = logs.stream().collect(Collectors.toMap(item -> item.getDate(), item -> item));
        // 构建时间数组
        List<String> days = getDays(getStartCreateTime(), getEndCreateTime(), "M.d");
        List<Integer> count = new ArrayList<>();
        List<Integer> success = new ArrayList<>();
        List<Integer> fail = new ArrayList<>();
        for (String day : days) {
            if (logsMap.containsKey(day)) {
                StatisticsCountVO statisticsCountVO = logsMap.get(day);
                count.add(statisticsCountVO.getCount());
                success.add(statisticsCountVO.getSuccess());
                fail.add(statisticsCountVO.getCount() - statisticsCountVO.getSuccess());
            } else {
                count.add(0);
                success.add(0);
                fail.add(0);
            }
        }

        Map result = new HashMap();
        result.put("count", count);
        result.put("success", success);
        result.put("fail", fail);
        result.put("date", days);
        return R.success(result);
    }

    @GetMapping("countForConfig")
    @ApiOperation("各通道成功量（条）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间", paramType = "query", required = true, dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间", paramType = "query", required = true, dataType = "String")
    })
    public R<List<Map>> countForConfig() {
        Map params = new HashMap();
        if (getStartCreateTime() != null) {
            params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        if (getEndCreateTime() != null) {
            params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }

        List<Map> countForConfig = sendLogService.countForConfig(params);

        return R.success(countForConfig);
    }

    @GetMapping("rateForConfig")
    @ApiOperation("各通道送达率")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间", paramType = "query", required = true, dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间", paramType = "query", required = true, dataType = "String")
    })
    public R<Map> rateForConfig() {
        Map params = new HashMap();
        if (getStartCreateTime() != null) {
            params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        if (getEndCreateTime() != null) {
            params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }

        List<Map> rateForConfig = sendLogService.rateForConfig(params);

        Map result = new HashMap();
        result.put("name", rateForConfig.stream().map(item -> item.get("name")).collect(Collectors.toList()));
        result.put("rate", rateForConfig.stream().map(item -> item.get("value")).collect(Collectors.toList()));
        return R.success(result);
    }

    @GetMapping("top10")
    @ApiOperation("应用发送数量TOP10")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间", paramType = "query", required = true, dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间", paramType = "query", required = true, dataType = "String")
    })
    public R top10() {
        Map params = new HashMap();
        if (getStartCreateTime() != null) {
            params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        if (getEndCreateTime() != null) {
            params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }


        LambdaQueryWrapper<ReceiveLogEntity> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.between((getStartCreateTime() != null && getEndCreateTime() != null), BaseEntity::getCreateTime, getStartCreateTime(), getEndCreateTime());
        int count = receiveLogService.count(countWrapper);
        countWrapper.eq(ReceiveLogEntity::getStatus, 1);// 成功
        int success = receiveLogService.count(countWrapper);

        List<StatisticsCountVO> logs = receiveLogService.top10(params);

        Map result = new HashMap();

        List<String> platformNames = new ArrayList<>();
        List<Integer> successList = new ArrayList<>();
        List<Integer> countList = new ArrayList<>();
        List<Integer> failList = new ArrayList<>();

        logs.forEach(item -> {
            platformNames.add(item.getDate());
            successList.add(item.getSuccess());
            countList.add(item.getCount());
            failList.add(item.getCount() - item.getSuccess());
        });

        result.put("platformNames", platformNames);
        result.put("success", successList);
        result.put("count", countList);
        result.put("fail", failList);

        result.put("dataInfo", new HashMap<String, Integer>() {{
            put("count", count);
            put("success", success);
            put("fail", count - success);
        }});

        return R.success(result);
    }

    @GetMapping("marketingTrend")
    @ApiOperation("营销短信发送量趋势")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间", paramType = "query", required = true, dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间", paramType = "query", required = true, dataType = "String")
    })
    public R marketingTrend() {
        // 营销平台id  先写死
        String platformId = "00000";
        Map params = new HashMap();
        if (getStartCreateTime() != null) {
            params.put("startCreateTime", DateUtils.format(getStartCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        if (getEndCreateTime() != null) {
            params.put("endCreateTime", DateUtils.format(getEndCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        }
        params.put("platformId", platformId);
        List<StatisticsCountVO> logs = receiveLogService.trend(params);
        Map<String, StatisticsCountVO> logsMap = logs.stream().collect(Collectors.toMap(item -> item.getDate(), item -> item));

        LambdaQueryWrapper<ReceiveLogEntity> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.between((getStartCreateTime() != null && getEndCreateTime() != null), BaseEntity::getCreateTime, getStartCreateTime(), getEndCreateTime());
        countWrapper.eq(ReceiveLogEntity::getPlatformId, platformId);
        int count = receiveLogService.count(countWrapper);
        countWrapper.eq(ReceiveLogEntity::getStatus, 1);// 成功
        int success = receiveLogService.count(countWrapper);

        // 构建时间数组
        List<String> days = getDays(getStartCreateTime(), getEndCreateTime(), "M.d");
        List<Integer> countArray = new ArrayList<>();
        List<Integer> successArray = new ArrayList<>();
        List<Integer> failArray = new ArrayList<>();
        for (String day : days) {
            if (logsMap.containsKey(day)) {
                StatisticsCountVO statisticsCountVO = logsMap.get(day);
                countArray.add(statisticsCountVO.getCount());
                successArray.add(statisticsCountVO.getSuccess());
                failArray.add(statisticsCountVO.getCount() - statisticsCountVO.getSuccess());
            } else {
                countArray.add(0);
                successArray.add(0);
                failArray.add(0);
            }
        }

        Map result = new HashMap();
        result.put("count", countArray);
        result.put("success", successArray);
        result.put("fail", failArray);
        result.put("date", days);
        result.put("dataInfo", new HashMap<String, Integer>() {{
            put("count", count);
            put("success", success);
            put("fail", count - success);
        }});
        return R.success(result);
    }
}
