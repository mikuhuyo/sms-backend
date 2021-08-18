package com.sms.manage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pd.core.base.BaseController;
import com.pd.core.base.R;
import com.pd.database.mybatis.conditions.Wraps;
import com.pd.database.mybatis.conditions.query.LbqWrapper;
import com.sms.dto.PlatformDTO;
import com.sms.entity.PlatformEntity;
import com.sms.manage.annotation.DefaultParams;
import com.sms.manage.service.PlatformService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


/**
 * 配置表
 */
@RestController
@RequestMapping("platform")
@Api(tags = {"接入平台"})
public class PlatformController extends BaseController {

    @Autowired
    private PlatformService platformService;

    /**
     * [查询] 分页查询
     *
     * @return
     * @author 传智播客
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
    public R<Page<PlatformEntity>> page(PlatformDTO platformDTO) {
        Page<PlatformEntity> page = getPage();
        LbqWrapper<PlatformEntity> wrapper = Wraps.lbQ();

        wrapper.like(StringUtils.isNotEmpty(platformDTO.getAccessKeyId()), PlatformEntity::getAccessKeyId, platformDTO.getAccessKeyId())
                .like(StringUtils.isNotEmpty(platformDTO.getAccessKeySecret()), PlatformEntity::getAccessKeySecret, platformDTO.getAccessKeySecret())
                .like(StringUtils.isNotEmpty(platformDTO.getIpAddr()), PlatformEntity::getIpAddr, platformDTO.getIpAddr())
                .like(StringUtils.isNotEmpty(platformDTO.getName()), PlatformEntity::getName, platformDTO.getName())
                .eq(platformDTO.getNeedAuth() != null, PlatformEntity::getNeedAuth, platformDTO.getNeedAuth())
                .eq(platformDTO.getIsActive() != null, PlatformEntity::getIsActive, platformDTO.getIsActive())
                .orderByDesc(PlatformEntity::getIsActive, PlatformEntity::getCreateTime);

        platformService.page(page, wrapper);
        return success(page);
    }

    @GetMapping("{id}")
    @ApiOperation("信息")
    public R<PlatformEntity> get(@PathVariable("id") String id) {
        PlatformEntity data = platformService.getById(id);

        return R.success(data);
    }

    @PostMapping
    @ApiOperation("保存")
    @DefaultParams
    public R save(@RequestBody PlatformEntity entity) {
        if (StringUtils.isBlank(entity.getAccessKeyId())) {
            entity.setAccessKeyId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (StringUtils.isBlank(entity.getAccessKeySecret())) {
            entity.setAccessKeySecret(UUID.randomUUID().toString().replace("-", ""));
        }
        if (platformService.getByName(entity.getName()) != null) {
            return R.fail("应用名称重复");
        }
        platformService.save(entity);

        return R.success();
    }

    @PutMapping
    @ApiOperation("修改")
    @DefaultParams
    public R update(@RequestBody PlatformEntity entity) {
        PlatformEntity platform = platformService.getByName(entity.getName());
        if (platform != null && !platform.getId().equals(entity.getId())) {
            return R.fail("应用名称重复");
        }

        platformService.updateById(entity);

        return R.success();
    }

    @DeleteMapping
    @ApiOperation("删除")
    public R delete(@RequestBody List<String> ids) {

        platformService.removeByIds(ids);

        return R.success();
    }

}
