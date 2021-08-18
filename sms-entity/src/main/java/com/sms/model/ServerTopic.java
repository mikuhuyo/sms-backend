package com.sms.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerTopic {
    public static final String INIT_CONNECT = "INIT_CONNECT";
    public static final String USE_NEW_CONNECT = "USE_NEW_CONNECT";

    private String option;
    private String value;

    public static ServerTopic load(String deserialize) {
        return JSON.parseObject(deserialize, ServerTopic.class);
    }

    public String toString() {
        return JSON.toJSONString(this);
    }
}
