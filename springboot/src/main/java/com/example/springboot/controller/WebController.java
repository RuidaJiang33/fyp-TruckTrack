package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/*
 * function:
 * author: Ruida
 * date: 2024/2/13 16:15
 */
@RestController
public class WebController {

    @Resource
    UserService userService;

    @AuthAccess
    @GetMapping("/")
    public Result hello() { return Result.success("success");}

    @AuthAccess
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            return Result.error("invalid input");
        }
        if (user.getUsername().length() > 20 || user.getPassword().length() > 20) {
            return Result.error("Username or password cannot exceed 20 characters");
        }
        user = userService.login(user);
        return Result.success(user);
    }

    @AuthAccess
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            return Result.error("invalid input");
        }
        user = userService.register(user);
        return Result.success(user);
    }

    // 重置密码
    @AuthAccess
    @PutMapping("/password")
    public Result password(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPhone())) {
            return Result.error("invalid input");
        }
        userService.resetPassword(user);
        return Result.success();
    }
}
