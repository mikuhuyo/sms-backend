package com.sms.server.config.datasource;

import com.pd.database.datasource.BaseMybatisConfiguration;
import com.pd.database.properties.DatabaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 配置一些拦截器
 */
@Slf4j
@Configuration
public class SmsServerMybatisAutoConfiguration extends BaseMybatisConfiguration {
    public SmsServerMybatisAutoConfiguration(DatabaseProperties databaseProperties) {
        super(databaseProperties);
    }
}
