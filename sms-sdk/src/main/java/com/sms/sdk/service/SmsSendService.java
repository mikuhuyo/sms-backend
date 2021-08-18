package com.sms.sdk.service;

import com.sms.sdk.dto.R;
import com.sms.sdk.dto.SmsBatchParamsDTO;
import com.sms.sdk.dto.SmsParamsDTO;

public interface SmsSendService {
    R sendSms(SmsParamsDTO smsParamsDTO);

    R batchSendSms(SmsBatchParamsDTO smsBatchParamsDTO);
}
