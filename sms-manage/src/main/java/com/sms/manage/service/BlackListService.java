package com.sms.manage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pd.core.base.R;
import com.sms.entity.BlackListEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * 黑名单
 *
 * @author 传智播客
 */
public interface BlackListService extends IService<BlackListEntity> {

    R<Boolean> upload(MultipartFile file);
}
