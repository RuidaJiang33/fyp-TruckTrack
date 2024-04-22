package com.example.springboot.service.aop;

/*
 * function:
 * author: Ruida
 * date: 2024/3/6 16:44
 */

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import com.example.springboot.common.UserLogs;
import com.example.springboot.entity.Logs;
import com.example.springboot.entity.User;
import com.example.springboot.service.LogsService;
import com.example.springboot.utils.IpUtils;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
@Slf4j
public class LogsAspect {

    @Resource
    LogsService logsService;

    @AfterReturning(pointcut = "@annotation(userLogs)", returning = "jsonResult")
    public void recordLog(JoinPoint joinPoint, UserLogs userLogs, Object jsonResult) {
        // 获取当前登录的用户的信息
        User loginUser = TokenUtils.getCurrentUser();
        if (loginUser == null) { // 用户未登录的情况下 loginUser为null 是null就从参数里面获取操作人信息
            // 登录 注册
            Object[] args = joinPoint.getArgs();
            if (ArrayUtil.isNotEmpty(args)) {
                if (args[0] instanceof User) {
                    loginUser = (User) args[0];
                }
            }
        }

        if (loginUser == null) {
            log.error("Logs info error, cannot get current user information");
            return;
        }

        // 获取HttpServletRequest对象
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        // 获取ip信息 组装日志的实体对象
        String ipAddr = IpUtils.getIpAddr(request);
        Logs logs = Logs.builder()
                .user(loginUser.getUsername())
                .operation(userLogs.operation())
                .type(userLogs.type().getValue())
                .ip(ipAddr)
                .time(DateUtil.now())
                .build();

        // 插入数据到数据库
        ThreadUtil.execAsync(() -> {
            logsService.save(logs);
        });
    }

}
