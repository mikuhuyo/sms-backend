package com.sms.server.job;

/**
 * 定时发送短信
 */
public interface SendTimingSms {
    void execute(String timing) throws InterruptedException;
}
