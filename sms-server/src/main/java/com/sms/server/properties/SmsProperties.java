package com.sms.server.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pd.sms")
@RefreshScope
public class SmsProperties {
    // 消息最大失败次数
    private int messageErrorNum;
    // 通道最大失败次数
    private int configLevelFailNum;
    // 通道选举算法启动比例
    private double configBuildScale;
}
