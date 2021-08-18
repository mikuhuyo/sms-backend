package com.sms.server.factory;

import com.alibaba.fastjson.JSON;
import com.pd.core.utils.SpringUtils;
import com.sms.entity.ConfigEntity;
import com.sms.model.ServerTopic;
import com.sms.server.config.RedisLock;
import com.sms.server.entity.SmsConfig;
import com.sms.server.service.ConfigService;
import com.sms.server.service.impl.SignatureServiceImpl;
import com.sms.server.service.impl.TemplateServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 通道的实例加载器
 * 执行时间:
 * 1.项目启动时
 * 2.通道重新排序时
 */
@Component
@Slf4j
@Order(value = 101)
public class SmsConnectLoader implements CommandLineRunner {

    private static final List<Object> CONNECT_LIST = new ArrayList<>();

    private static String BUILD_NEW_CONNECT_TOKEN = null;

    private static List<ConfigEntity> FUTURE_CONFIG_LIST;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void run(String... args) {
        initConnect();
    }

    /**
     * 根据通道配置, 初始化每个通道的bean对象
     */
    @SneakyThrows
    public void initConnect() {
        //获得通道列表
        List<ConfigEntity> configs = configService.listForConnect();
        List<Object> connectList = new ArrayList<>();
        //遍历通道列表
        for (ConfigEntity config : configs) {
            try {
                //通道配置
                SmsConfig smsConfig = new SmsConfig();
                smsConfig.setId(config.getId());
                smsConfig.setDomain(config.getDomain().trim());
                smsConfig.setName(config.getName().trim());
                smsConfig.setPlatform(config.getPlatform().trim());
                smsConfig.setAccessKeyId(config.getAccessKeyId().trim());
                smsConfig.setAccessKeySecret(config.getAccessKeySecret().trim());
                if (StringUtils.isNotBlank(config.getOther())) {
                    LinkedHashMap linkedHashMap = JSON.parseObject(config.getOther(), LinkedHashMap.class);
                    smsConfig.setOtherConfig(linkedHashMap);
                }
                //如: com.pd.sms.AliyunSmsService
                //也就是说当定义新的通道时, 类的名称需要以SmsService结尾, 通道名称需要保存在数据库中
                Class cls = Class.forName("com.sms.sms." + config.getPlatform() + "SmsService");

                Constructor<?> cons = cls.getConstructor(SmsConfig.class);
                Object object = cons.newInstance(smsConfig);
                //签名
                Field signatureServiceField = cls.getSuperclass().getDeclaredField("signatureService");

                //模板
                Field templateServiceField = cls.getSuperclass().getDeclaredField("templateService");

                signatureServiceField.setAccessible(true);
                templateServiceField.setAccessible(true);
                signatureServiceField.set(object, SpringUtils.getBean(SignatureServiceImpl.class));
                templateServiceField.set(object, SpringUtils.getBean(TemplateServiceImpl.class));

                connectList.add(object);
                log.info("通道初始化成功: {} {}", config.getName(), config.getPlatform());
            } catch (Exception e) {
                log.warn("通道初始化失败: {}", e.getMessage());
            }
        }

        //重新赋值通道列表
        if (!CollectionUtils.isEmpty(connectList)) {
            CONNECT_LIST.clear();
            CONNECT_LIST.addAll(connectList);
        }

        //解锁
        if (StringUtils.isNotBlank(BUILD_NEW_CONNECT_TOKEN)) {
            redisLock.unlock("buildNewConnect", BUILD_NEW_CONNECT_TOKEN);
        }
        log.info("通道初始化完成:{}", CONNECT_LIST);
    }

    public <T> T getConnectByLevel(Integer level) {
        return (T) CONNECT_LIST.get(level - 1);
    }

    public boolean checkConnectLevel(Integer level) {
        return CONNECT_LIST.size() <= level;
    }

    /**
     * 通道调整:
     * 通道初始化: 构建新的通道配置
     * 只能有一台机器执行, 所以需要加锁
     */
    public void buildNewConnect() {
        // 一小时内有效
        String token = redisLock.tryLock("buildNewConnect", 1000 * 60 * 60 * 1);
        log.info("buildNewConnect token:{}", token);
        if (StringUtils.isNotBlank(token)) {
            List<ConfigEntity> list = configService.listForNewConnect();
            FUTURE_CONFIG_LIST = list;
            redisTemplate.opsForValue().set("NEW_CONNECT_SERVER", ServerRegister.SERVER_ID);
            BUILD_NEW_CONNECT_TOKEN = token;
        }
        // 获取不到锁 证明已经有服务在计算或者计算结果未得到使用
    }

    /**
     * 通道调整:
     * 发布订阅消息, 通知其他服务: 应用新的通道
     */
    public void changeNewConnectMessage() {
        redisTemplate.convertAndSend("TOPIC_HIGH_SERVER", ServerTopic.builder().option(ServerTopic.USE_NEW_CONNECT).value(ServerRegister.SERVER_ID).build().toString());
    }

    /**
     * 通道调整
     * 发布订阅消息, 通知其他服务: 初始化新通道
     */
    public void changeNewConnect() {
        // 初始化通道
        Object newConnectServer = redisTemplate.opsForValue().get("NEW_CONNECT_SERVER");

        /**
         * 为了通道调整发布的消息中, 带有server id
         * 确保只有此server id的服务执行当前代码
         */
        if (null != newConnectServer && ServerRegister.SERVER_ID.equals(newConnectServer) &&
                !CollectionUtils.isEmpty(FUTURE_CONFIG_LIST)) {
            // 配置列表不为空则执行数据库操作 并清空缓存
            boolean result = configService.updateBatchById(FUTURE_CONFIG_LIST);
            log.info("批量修改配置级别:{}", result);
            FUTURE_CONFIG_LIST.clear();
            redisTemplate.convertAndSend("TOPIC_HIGH_SERVER", ServerTopic.builder().option(ServerTopic.INIT_CONNECT).value(ServerRegister.SERVER_ID).build().toString());
        }
    }
}
