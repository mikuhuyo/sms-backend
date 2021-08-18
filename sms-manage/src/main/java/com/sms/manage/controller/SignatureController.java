package com.sms.manage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pd.core.base.BaseController;
import com.pd.core.base.R;
import com.pd.database.mybatis.conditions.Wraps;
import com.pd.database.mybatis.conditions.query.LbqWrapper;
import com.sms.dto.SignatureDTO;
import com.sms.entity.ReceiveLogEntity;
import com.sms.entity.SignatureEntity;
import com.sms.manage.annotation.DefaultParams;
import com.sms.manage.service.ReceiveLogService;
import com.sms.manage.service.SignatureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签名表
 */
@RestController
@RequestMapping("signature")
@Api(tags = "签名表")
public class SignatureController extends BaseController {
    @Autowired
    private SignatureService signatureService;
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
    public R<Page<SignatureEntity>> page(SignatureDTO signatureDTO) {
        Page<SignatureEntity> page = getPage();
        LbqWrapper<SignatureEntity> wrapper = Wraps.lbQ();

        wrapper.like(SignatureEntity::getName, signatureDTO.getName())
                .like(SignatureEntity::getCode, signatureDTO.getCode())
                .like(SignatureEntity::getContent, signatureDTO.getContent())
                .orderByDesc(SignatureEntity::getCreateTime);

        signatureService.page(page, wrapper);
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
    public R<Page<SignatureDTO>> customPage(SignatureDTO signatureDTO) {
        Page<SignatureDTO> page = getPage();

        Map params = new HashMap();
        params.put("name", signatureDTO.getName());
        params.put("code", signatureDTO.getCode());
        params.put("configId", signatureDTO.getConfigId());

        signatureService.customPage(page, params);
        return R.success(page);
    }

    @GetMapping("list")
    @ApiOperation("全部")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "排序字段", value = "排序字段", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "排序方式", value = "排序方式, 可选值(asc.desc)", paramType = "query", dataType = "String")
    })
    public R<List<SignatureEntity>> list(SignatureDTO signatureDTO) {
        LbqWrapper<SignatureEntity> wrapper = Wraps.lbQ();

        wrapper.like(SignatureEntity::getName, signatureDTO.getName())
                .like(SignatureEntity::getCode, signatureDTO.getCode())
                .like(SignatureEntity::getContent, signatureDTO.getContent())
                .orderByDesc(SignatureEntity::getCreateTime);

        List<SignatureEntity> list = signatureService.list(wrapper);
        return R.success(list);
    }

    @GetMapping("{id}")
    @ApiOperation("信息")
    public R<SignatureEntity> get(@PathVariable("id") String id) {
        SignatureEntity data = signatureService.getById(id);

        return R.success(data);
    }

    @PostMapping
    @ApiOperation("保存")
    @DefaultParams
    public R save(@RequestBody SignatureEntity entity) {
        String code = signatureService.getNextCode();
        entity.setCode(code);
        entity.setContent(entity.getName());
        if (signatureService.getByName(entity.getName()) != null) {
            return R.fail("模板名称重复");
        }
        signatureService.save(entity);
        return R.success();
    }

    @PutMapping
    @ApiOperation("修改")
    @DefaultParams
    public R update(@RequestBody SignatureEntity entity) {
        SignatureEntity signature = signatureService.getByName(entity.getName());
        if (signature != null && !signature.getId().equals(entity.getId())) {
            return R.fail("模板名称重复");
        }
        signatureService.updateById(entity);

        return R.success();
    }

    @DeleteMapping
    @ApiOperation("删除")
    public R delete(@RequestBody List<String> ids) {

        List<String> codes = new ArrayList<>();
        List<String> nids = new ArrayList<>();

        for (String id : ids) {
            SignatureEntity signature = signatureService.getById(id);
            if (signature == null) {
                continue;
            }

            LambdaQueryWrapper<ReceiveLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReceiveLogEntity::getSignature, signature.getCode());
            List<ReceiveLogEntity> logs = receiveLogService.list(wrapper);
            if (logs.size() > 0) {
                // 已使用过无法删除
                codes.add(signature.getCode());
            } else {
                nids.add(id);
            }
        }

        if (nids.size() > 0) {
            signatureService.removeByIds(nids);
        }

        return R.success(codes);
    }
}
