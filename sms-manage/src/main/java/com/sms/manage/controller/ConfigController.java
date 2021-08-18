package com.sms.manage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pd.core.base.BaseController;
import com.pd.core.base.R;
import com.pd.database.mybatis.conditions.Wraps;
import com.pd.database.mybatis.conditions.query.LbqWrapper;
import com.sms.dto.ConfigDTO;
import com.sms.dto.ConfigUpdateOtherDTO;
import com.sms.dto.SignatureDTO;
import com.sms.dto.TemplateDTO;
import com.sms.entity.ConfigEntity;
import com.sms.entity.ConfigSignatureEntity;
import com.sms.entity.ConfigTemplateEntity;
import com.sms.entity.base.BaseEntity;
import com.sms.manage.annotation.DefaultParams;
import com.sms.manage.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 配置表
 */
@RestController
@RequestMapping("config")
@Api(tags = "通道配置")
public class ConfigController extends BaseController {

    @Autowired
    private ConfigService configService;
    @Autowired
    private ConfigSignatureService configSignatureService;
    @Autowired
    private SignatureService signatureService;
    @Autowired
    private ConfigTemplateService configTemplateService;
    @Autowired
    private TemplateService templateService;


    /**
     * [查询] 分页查询
     *
     * @return
     */
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
    public R<Page<ConfigEntity>> page(ConfigDTO configDTO) {
        Page<ConfigEntity> page = getPage();
        LbqWrapper<ConfigEntity> wrapper = Wraps.lbQ();

        //构建分页查询条件
        wrapper.like(ConfigEntity::getName, configDTO.getName())
                .like(ConfigEntity::getAccessKeyId, configDTO.getAccessKeyId())
                .like(ConfigEntity::getAccessKeySecret, configDTO.getAccessKeySecret())
                .eq(ConfigEntity::getChannelType, configDTO.getChannelType())
                .eq(ConfigEntity::getPlatform, configDTO.getPlatform())
                .eq(ConfigEntity::getDomain, configDTO.getDomain())
                .eq(ConfigEntity::getIsActive, configDTO.getIsActive())
                .eq(ConfigEntity::getIsEnable, configDTO.getIsEnable())
                .orderByAsc(ConfigEntity::getLevel);

        configService.page(page, wrapper);
        return success(page);
    }

    /**
     * 根据id查询通道信息
     */
    @GetMapping("{id}")
    @ApiOperation("信息")
    public R<ConfigDTO> get(@PathVariable("id") String id) {

        ConfigEntity data = configService.getById(id);
        if (null == data || StringUtils.isEmpty(data.getId())) {
            return R.fail("Id Not Found");
        }
        ConfigDTO configDTO = new ConfigDTO();
        BeanUtils.copyProperties(data, configDTO);

        Map params = new HashMap();
        params.put("configId", id);
        List<SignatureDTO> signatureDtos = signatureService.customList(params);
        //configDTO.setSignatureDTOS(signatureDtos);
        configDTO.setSignatureIds(signatureDtos.stream().map(item -> item.getId()).collect(Collectors.toList()));

        List<TemplateDTO> templateDtos = templateService.customList(params);
        //configDTO.setTemplateDTOS(templateDtos);
        configDTO.setTemplateIds(templateDtos.stream().map(item -> item.getId()).collect(Collectors.toList()));

        return R.success(configDTO);
    }

    @PostMapping
    @ApiOperation("保存")
    @DefaultParams
    public R save(@RequestBody ConfigDTO entity) {
        if (configService.getByName(entity.getName()) != null) {
            return R.fail("通道名称重复");
        }

        //获取通道优先级
        configService.getNewLevel(entity);

        //保存通道信息
        configService.save(entity);

        //通知短信发送服务.更新通道优先级
        configService.sendUpdateMessage();

        return R.success();
    }

    @PostMapping("level")
    @ApiOperation("排序")
    public R level(@RequestBody List<String> ids) {
        for (int i = 0; i < ids.size(); i++) {
            LambdaUpdateWrapper<ConfigEntity> updateWrapper = new LambdaUpdateWrapper();
            updateWrapper.eq(BaseEntity::getId, ids.get(i));
            updateWrapper.eq(ConfigEntity::getIsEnable, 1);
            updateWrapper.set(ConfigEntity::getLevel, (i + 1));
            configService.update(updateWrapper);
        }
        configService.sendUpdateMessage();
        return R.success();
    }


    @PutMapping
    @ApiOperation("修改")
    @DefaultParams
    public R update(@RequestBody ConfigDTO entity) {
        ConfigEntity config = configService.getByName(entity.getName());
        if (config != null && !config.getId().equals(entity.getId())) {
            return R.fail("通道名称重复");
        }

        //根据id修改配置信息
        configService.updateById(entity);

        //更新通道和签名关系
        configSignatureService.merge(entity);

        //更新通道和短信模板关系
        configTemplateService.merge(entity);

        //通知短信发送服务修改通道优先级
        configService.sendUpdateMessage();

        return R.success();
    }

    @PutMapping("other")
    @ApiOperation("关联内修改")
    public R updateOther(@RequestBody ConfigUpdateOtherDTO dto) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dto.getSignatureId())) {
            LambdaUpdateWrapper<ConfigSignatureEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ConfigSignatureEntity::getConfigId, dto.getConfigId());
            wrapper.eq(ConfigSignatureEntity::getSignatureId, dto.getSignatureId());
            wrapper.set(ConfigSignatureEntity::getConfigSignatureCode, dto.getConfigSignatureCode());
            configSignatureService.update(wrapper);
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dto.getTemplateId())) {
            LambdaUpdateWrapper<ConfigTemplateEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ConfigTemplateEntity::getConfigId, dto.getConfigId());
            wrapper.eq(ConfigTemplateEntity::getTemplateId, dto.getTemplateId());
            wrapper.set(ConfigTemplateEntity::getConfigTemplateCode, dto.getConfigTemplateCode());
            configTemplateService.update(wrapper);
        }
        return R.success();
    }

    @DeleteMapping
    @ApiOperation("删除")
    public R delete(@RequestBody List<String> ids) {
        configService.removeByIds(ids);

        ids.forEach(item -> {
            LambdaQueryWrapper<ConfigSignatureEntity> signatureWrapper = new LambdaQueryWrapper<>();
            signatureWrapper.eq(ConfigSignatureEntity::getConfigId, item);
            configSignatureService.remove(signatureWrapper);

            LambdaQueryWrapper<ConfigTemplateEntity> templateWrapper = new LambdaQueryWrapper<>();
            templateWrapper.eq(ConfigTemplateEntity::getConfigId, item);
            configTemplateService.remove(templateWrapper);
        });

        configService.sendUpdateMessage();
        return R.success();
    }

}
