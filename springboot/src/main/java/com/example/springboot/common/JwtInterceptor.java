package com.example.springboot.common;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/*
 * function:
 * author: Ruida
 * date: 2024/2/23 12:36
 */

public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token"); // header里面传过来的参数
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token"); // url参数 ?token=xxx
        }
        // 如果不是映射到方法直接跳过
        if (handler instanceof HandlerMethod) {
            AuthAccess annotation = ((HandlerMethod) handler).getMethodAnnotation(AuthAccess.class);
            if (annotation != null) {
                return true;
            }
        }
        // 执行验证
        if (StrUtil.isBlank(token)) {
            throw new ServiceException("401", "Please Login");
        }
        // 获取token中的user id
        String userId;
        try {
            userId = JWT.decode(token).getAudience().get(0); // JWT.decode(token) 解码 jwt token
        } catch (JWTDecodeException j) {
            throw new ServiceException("401", "Please Login");
        }
        // 根据token中的userid查询数据库
        User user = userMapper.selectById(Integer.valueOf(userId));
        if (user == null) {
            throw new ServiceException("401", "Please Login");
        }
        // 用户名密码加密后生成一个验证器
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
        try {
            jwtVerifier.verify(token); // 验证token
        } catch (JWTVerificationException e) {
            throw new ServiceException("401","Please Login");
        }
        return true;
    }
}
