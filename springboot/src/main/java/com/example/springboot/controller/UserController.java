package com.example.springboot.controller;

/*
 * function:
 * author: Ruida
 * date: 2024/2/14 10:11
 */
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.LogType;
import com.example.springboot.common.Result;
import com.example.springboot.common.UserLogs;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.service.UserService;
import com.example.springboot.utils.TokenUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @UserLogs(operation = "User", type = LogType.ADD)
    @PostMapping("/add")
    public Result add(@RequestBody User user){
        try {
            userService.save(user);
        } catch (Exception e){
            if( e instanceof DuplicateKeyException){
                return Result.error("401","Bad insert");
            } else {
                return Result.error("500","System error");
            }
        }
        return Result.success();
    }

    @UserLogs(operation = "User", type = LogType.UPDATE)
    @PutMapping("/update")
    public Result update(@RequestBody User user){
        userService.updateById(user);
        return Result.success();
    }

    @UserLogs(operation = "User", type = LogType.DELETE)
    @DeleteMapping("/delete/{uid}")
    public Result delete(@PathVariable Integer uid){
        User currentUser = TokenUtils.getCurrentUser();
        if (uid.equals(currentUser.getUid())) {
            throw new ServiceException("Cannot delete current user");
        }
        userService.removeById(uid);
        return Result.success();
    }

    @UserLogs(operation = "User", type = LogType.BATCH_DELETE)
    @DeleteMapping("/delete/batch")
    public Result batchDelete(@RequestBody List<Integer> uids){
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser != null && currentUser.getUid() != null && uids.contains(currentUser.getUid())) {
            throw new ServiceException("Cannot delete current user");
        }
        userService.removeBatchByIds(uids);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<User> userList = userService.list(new QueryWrapper<User>().orderByDesc("uid"));
        return Result.success(userList);
    }

    @GetMapping("/selectByUid/{uid}")
    public Result selectByUid(@PathVariable Integer uid) {
        User user = userService.getById(uid);
        return Result.success(user);
    }

    @GetMapping("/selectByUsername/{username}")
    public Result selectByUsername(@PathVariable String username) {
        User user = userService.selectByUsername(username);
        return Result.success(user);
    }

    /**
    * pageNum: The number of current page
    * pageSize: The size of each page
    */
    @GetMapping("/selectByPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String username,
                               @RequestParam String name) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().orderByDesc("uid");
        queryWrapper.like(StrUtil.isNotBlank(username),"username", username);
        queryWrapper.like(StrUtil.isNotBlank(name),"name", name);
        // select * from user where username like '%#{username}%' and name like '%#{name}%'
        Page<User> page = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }

    /**
    * 批量导出数据
    * */
    @GetMapping("/export")
    public void exportData(@RequestParam(required = false) String username,
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) String uids,
                           HttpServletResponse response) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        List<User> list;
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(uids)){
            List<Integer> uidsArr = Arrays.stream(uids.split(",")).map(Integer::valueOf).collect(Collectors.toList());
            queryWrapper.in("uid", uidsArr);
        } else {
            // 全部导出，或条件导出
            queryWrapper.like(StrUtil.isNotBlank(username),"username", username);
            queryWrapper.like(StrUtil.isNotBlank(name),"name", name);
        }
        list = userService.list(queryWrapper); // 查询出当前User表的所有数据
        writer.write(list, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Dispositon", "attachment;filename=" + URLEncoder.encode("Users info", "UTF-8") + ".xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        writer.flush(outputStream, true); // 将数据刷新到输出流，在刷新后关闭输出流
        writer.close();
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 批量导入
     * @param file 传入的excel对象
     * @return 导入结果
     * */
    @PostMapping("/import")
    public Result importData(MultipartFile file) throws IOException {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<User> userList = reader.readAll(User.class);
        // 写入数据到数据库
        try {
            userService.saveBatch(userList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("File import error");
        }
        return Result.success();
    }
}
