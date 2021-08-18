package com.sms.manage.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pd.core.base.BaseController;
import com.pd.core.base.R;
import com.pd.database.mybatis.conditions.Wraps;
import com.pd.database.mybatis.conditions.query.LbqWrapper;
import com.sms.dto.TemplateDTO;
import com.sms.entity.ReceiveLogEntity;
import com.sms.entity.TemplateEntity;
import com.sms.manage.annotation.DefaultParams;
import com.sms.manage.service.ReceiveLogService;
import com.sms.manage.service.TemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板表
 */
@RestController
@RequestMapping("template")
@Api(tags = "模板表")
public class TemplateController extends BaseController {
    @Autowired
    private TemplateService templateService;
    @Autowired
    private ReceiveLogService receiveLogService;

    @GetMapping("page")
    @ApiOperation("分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页码, 从1开始", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页显示记录数", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "排序方式", value = "排序方式, 可选值(asc.desc)", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String")
    })
    public R<Page<TemplateEntity>> page(TemplateDTO templateDTO) {
        Page<TemplateEntity> page = getPage();
        LbqWrapper<TemplateEntity> wrapper = Wraps.lbQ();

        wrapper.like(TemplateEntity::getName, templateDTO.getName())
                .like(TemplateEntity::getCode, templateDTO.getCode())
                .like(TemplateEntity::getContent, templateDTO.getContent())
                .orderByDesc(TemplateEntity::getCreateTime);

        templateService.page(page, wrapper);
        return R.success(page);
    }

    @GetMapping("customPage")
    @ApiOperation("自定义分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页码, 从1开始", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页显示记录数", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "排序方式", value = "排序方式, 可选值(asc.desc)", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startCreateTime", value = "开始时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endCreateTime", value = "结束时间（yyyy-MM-dd HH:mm:ss）", paramType = "query", dataType = "String")
    })
    public R<Page<TemplateDTO>> customPage(TemplateDTO templateDTO) {
        Page<TemplateDTO> page = getPage();

        Map params = new HashMap();
        params.put("name", templateDTO.getName());
        params.put("code", templateDTO.getCode());
        params.put("configId", templateDTO.getConfigId());

        templateService.customPage(page, params);
        return R.success(page);
    }

    @GetMapping("list")
    @ApiOperation("全部")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "排序方式", value = "排序方式, 可选值(asc.desc)", paramType = "query", dataType = "String")
    })
    public R<List<TemplateEntity>> list(TemplateDTO templateDTO) {
        LbqWrapper<TemplateEntity> wrapper = Wraps.lbQ();

        wrapper.like(TemplateEntity::getName, templateDTO.getName())
                .like(TemplateEntity::getCode, templateDTO.getCode())
                .like(TemplateEntity::getContent, templateDTO.getContent())
                .orderByDesc(TemplateEntity::getCreateTime);

        List<TemplateEntity> list = templateService.list(wrapper);
        return R.success(list);
    }

    @GetMapping("{id}")
    @ApiOperation("信息")
    public R<TemplateEntity> get(@PathVariable("id") String id) {
        TemplateEntity data = templateService.getById(id);

        return R.success(data);
    }

    @GetMapping("paramFields")
    @ApiOperation("获取参数信息")
    public R paramFields(@RequestParam String code) {
        TemplateEntity template = templateService.getByCode(code);
        char[] charArray = template.getContent().toCharArray();
        boolean flag = false;
        List<String> fields = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == '{' && i >= 1 && charArray[i - 1] == '$') {
                flag = true;
                continue;
            }
            if (charArray[i] == '}' && flag) {
                fields.add(stringBuffer.toString());
                stringBuffer.setLength(0);
                flag = false;
                continue;
            }
            if (flag) {
                stringBuffer.append(charArray[i]);
            }
        }
        Map result = new HashMap();
        result.put("data", fields);
        result.put("content", template.getContent());
        return R.success(result);
    }

    @PostMapping
    @ApiOperation("保存")
    @DefaultParams
    public R save(@RequestBody TemplateEntity entity) {
        String code = templateService.getNextCode();
        entity.setCode(code);
        if (templateService.getByName(entity.getName()) != null) {
            return R.fail("模板名称重复");
        }
        templateService.save(entity);

        return R.success();
    }

    @PostMapping("build")
    @ApiOperation("构建模板内容")
    @DefaultParams
    public R<String> build(@RequestBody TemplateEntity entity) {
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isNotBlank(entity.getContent()) && entity.getContent().startsWith("[")) {
            JSONArray jsonArray = JSON.parseArray(entity.getContent());
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject.putAll(jsonArray.getJSONObject(i));
            }
        }
        TemplateEntity templateEntity = templateService.getByCode(entity.getCode());
        if (templateEntity == null || CollectionUtils.isEmpty(jsonObject) || StringUtils.isBlank(templateEntity.getContent())) {
            return R.fail("参数异常");
        }
        String content = templateEntity.getContent();
        for (String key : jsonObject.keySet()) {
            content = content.replaceAll("\\$\\{" + key + "}", jsonObject.getString(key));
        }
        return R.success(content);
    }

    @PutMapping
    @ApiOperation("修改")
    @DefaultParams
    public R update(@RequestBody TemplateEntity entity) {

        TemplateEntity template = templateService.getByName(entity.getName());
        if (template != null && !template.getId().equals(entity.getId())) {
            return R.fail("模板名称重复");
        }
        templateService.updateById(entity);

        return R.success();
    }

    @DeleteMapping
    @ApiOperation("删除")
    public R delete(@RequestBody List<String> ids) {

        List<String> codes = new ArrayList<>();
        List<String> nids = new ArrayList<>();

        for (String id : ids) {
            TemplateEntity template = templateService.getById(id);
            if (template == null) {
                continue;
            }

            LambdaQueryWrapper<ReceiveLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReceiveLogEntity::getTemplate, template.getCode());
            List<ReceiveLogEntity> logs = receiveLogService.list(wrapper);
            if (logs.size() > 0) {
                // 已使用过无法删除
                codes.add(template.getCode());
            } else {
                nids.add(id);
            }
        }

        if (nids.size() > 0) {
            templateService.removeByIds(nids);
        }

        return R.success(codes);
    }
}
