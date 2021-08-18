package com.sms.manage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pd.core.base.BaseController;
import com.pd.core.base.R;
import com.pd.database.mybatis.conditions.Wraps;
import com.pd.database.mybatis.conditions.query.LbqWrapper;
import com.sms.dto.BlackListDTO;
import com.sms.entity.BlackListEntity;
import com.sms.manage.annotation.DefaultParams;
import com.sms.manage.service.BlackListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static com.pd.core.exception.code.ExceptionCode.BASE_VALID_PARAM;

/**
 * 黑名单
 */
@RestController
@RequestMapping("blacklist")
@Api(tags = "黑名单")
public class BlackListController extends BaseController {
    @Autowired
    private BlackListService blackListService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据条件分页查询黑名单
     *
     * @param blackListDTO
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
    public R<Page<BlackListEntity>> page(BlackListDTO blackListDTO) {
        Page<BlackListEntity> page = getPage();
        LbqWrapper<BlackListEntity> wrapper = Wraps.lbQ();
        //构建查询条件
        wrapper.like(BlackListEntity::getContent, blackListDTO.getContent())
                .like(BlackListEntity::getType, blackListDTO.getType())
                .orderByDesc(BlackListEntity::getCreateTime);
        //执行查询
        blackListService.page(page, wrapper);
        return R.success(page);
    }

    @GetMapping("{id}")
    @ApiOperation("信息")
    public R<BlackListEntity> get(@PathVariable("id") String id) {
        BlackListEntity data = blackListService.getById(id);
        return R.success(data);
    }

    @PostMapping
    @ApiOperation("保存")
    @DefaultParams
    public R save(@RequestBody BlackListEntity entity) {

        blackListService.save(entity);

        redisTemplate.delete("Black_" + 1);

        return R.success();
    }

    @PutMapping
    @ApiOperation("修改")
    @DefaultParams
    public R update(@RequestBody BlackListEntity entity) {

        blackListService.updateById(entity);

        redisTemplate.delete("Black_" + 1);

        return R.success();
    }

    @DeleteMapping
    @ApiOperation("删除")
    public R delete(@RequestBody List<String> ids) {

        blackListService.removeByIds(ids);

        redisTemplate.delete("Black_" + 1);

        return R.success();
    }

    @PostMapping("upload")
    @ApiOperation("导入")
    public R<? extends Object> upload(@RequestParam(value = "file") MultipartFile file) {
        if (file.isEmpty()) {
            return fail(BASE_VALID_PARAM.build("导入内容为空"));
        }
        R<Boolean> res = blackListService.upload(file);
        redisTemplate.delete("Black_" + 1);
        return res;
    }

    @GetMapping("export")
    @ApiOperation("导出")
    public void export(@ApiIgnore @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {

    }
}
