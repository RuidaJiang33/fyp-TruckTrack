package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Logs;
import com.example.springboot.entity.User;
import com.example.springboot.service.LogsService;
import com.example.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogsController {
    @Autowired
    LogsService logsService;

    @Autowired
    UserService userService;

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        logsService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result batchDelete(@RequestBody List<Integer> ids){
        logsService.removeBatchByIds(ids);
        return Result.success();
    }

    /**
     * pageNum: The number of current page
     * pageSize: The size of each page
     */
    @GetMapping("/selectByPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String operation,
                               @RequestParam String type,
                               @RequestParam String user) {
        QueryWrapper<Logs> queryWrapper = new QueryWrapper<Logs>().orderByDesc("id");
        queryWrapper.like(StrUtil.isNotBlank(operation),"operation", operation);
        queryWrapper.like(StrUtil.isNotBlank(type),"type", type);
        queryWrapper.like(StrUtil.isNotBlank(user),"user", user);
        Page<Logs> page = logsService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }
}
