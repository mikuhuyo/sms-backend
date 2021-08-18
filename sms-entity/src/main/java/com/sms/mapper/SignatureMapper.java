package com.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sms.dto.SignatureDTO;
import com.sms.entity.SignatureEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 签名表
 */
@Repository
public interface SignatureMapper extends BaseMapper<SignatureEntity> {

    IPage<SignatureDTO> custom(Page<SignatureDTO> page, @Param("params") Map params);

    List<SignatureDTO> custom(@Param("params") Map params);
}
