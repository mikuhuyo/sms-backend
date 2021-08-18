package com.sms.api.service;

import com.sms.api.dto.SmsBatchParamsDTO;
import com.sms.api.dto.SmsParamsDTO;

public interface SmsSendService {
    void send(SmsParamsDTO smsParamsDTO);

    void batchSend(SmsBatchParamsDTO smsBatchParamsDTO);
}
