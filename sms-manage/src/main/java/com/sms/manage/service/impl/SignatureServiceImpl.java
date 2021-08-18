package com.sms.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sms.dto.SignatureDTO;
import com.sms.entity.SignatureEntity;
import com.sms.entity.base.BaseEntity;
import com.sms.manage.service.SignatureService;
import com.sms.manage.utils.StringUtils;
import com.sms.mapper.SignatureMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 签名表
 */
@Service
public class SignatureServiceImpl extends ServiceImpl<SignatureMapper, SignatureEntity> implements SignatureService {

    @Override
    public String getNextCode() {
        LambdaQueryWrapper<SignatureEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BaseEntity::getCreateTime);
        wrapper.last(" limit 1");
        SignatureEntity entity = this.baseMapper.selectOne(wrapper);
        if (entity != null) {
            String code = entity.getCode();
            if (code.startsWith("DXQM")) {
                String numStr = code.split("_")[1];
                int num = Integer.parseInt(numStr) + 1;
                return "DXQM_" + StringUtils.autoGenericCode(num, 9);
            }
        }
        return "DXQM_000000001";
    }

    @Override
    public IPage<SignatureDTO> customPage(Page<SignatureDTO> page, Map params) {
        IPage<SignatureDTO> signatureDTOIPage = this.baseMapper.custom(page, params);
        page.setRecords(signatureDTOIPage.getRecords());
        return signatureDTOIPage;
    }

    @Override
    public List<SignatureDTO> customList(Map params) {
        List<SignatureDTO> signatureDTOS = this.baseMapper.custom(params);
        return signatureDTOS;
    }

    @Override
    public SignatureEntity getByCode(String code) {
        LambdaQueryWrapper<SignatureEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignatureEntity::getCode, code);
        SignatureEntity entity = this.baseMapper.selectOne(wrapper);
        return entity;
    }

    @Override
    public SignatureEntity getByName(String name) {
        LambdaQueryWrapper<SignatureEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignatureEntity::getName, name);
        SignatureEntity entity = this.baseMapper.selectOne(wrapper);
        return entity;
    }
}
