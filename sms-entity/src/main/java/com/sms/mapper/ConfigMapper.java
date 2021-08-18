package com.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sms.entity.ConfigEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 配置表
 */
@Repository
public interface ConfigMapper extends BaseMapper<ConfigEntity> {

    /**
     * 根据签名与模板 获取适配的通道配置信息
     *
     * @param params 签名id, 模板id
     * @return 配置集合
     */
    List<ConfigEntity> findByTemplateSignature(Map params);
}
