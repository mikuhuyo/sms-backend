package com.sms.manage.aspect;

import com.pd.core.context.BaseContextHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 通过切面方式, 自定义注解, 实现实体基础数据的注入（创建者.创建时间.修改者.修改时间）
 */
@Component
@Aspect
@Slf4j
public class DefaultParamsAspect {
    @SneakyThrows
    @Before("@annotation(com.sms.manage.annotation.DefaultParams)")
    public void beforeEvent(JoinPoint point) {
        // 从threadlocal中获取用户id
        Long userId = BaseContextHandler.getUserId();
        if (userId == null) {
            userId = 0L;
        }
        Object[] args = point.getArgs();
        for (int i = 0; i < args.length; i++) {
            Class<?> classes = args[i].getClass();
            Object id = null;
            Method method = getMethod(classes, "getId");
            if (null != method) {
                id = method.invoke(args[i]);
            }

            // 请求操作的对象的id为空时 为创建操作
            if (null == id) {
                method = getMethod(classes, "setCreateUser", String.class);
                if (null != method) {
                    method.invoke(args[i], userId.toString());
                }
                method = getMethod(classes, "setCreateTime", LocalDateTime.class);
                if (null != method) {
                    method.invoke(args[i], LocalDateTime.now());
                }
            }

            // 新建修改更新
            method = getMethod(classes, "setUpdateUser", String.class);
            if (null != method) {
                method.invoke(args[i], userId.toString());
            }
            method = getMethod(classes, "setUpdateTime", LocalDateTime.class);
            if (null != method) {
                method.invoke(args[i], LocalDateTime.now());
            }
        }
    }


    /**
     * 获得方法对象
     *
     * @param classes
     * @param name
     * @param types
     * @return
     */
    private Method getMethod(Class classes, String name, Class... types) {
        try {
            return classes.getMethod(name, types);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
