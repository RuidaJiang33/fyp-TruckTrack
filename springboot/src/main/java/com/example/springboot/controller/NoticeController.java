package com.example.springboot.controller;

/*
 * function:
 * author: Ruida
 * date: 2024/2/14 10:11
 */

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.LogType;
import com.example.springboot.common.Result;
import com.example.springboot.common.UserLogs;
import com.example.springboot.entity.Notice;
import com.example.springboot.entity.User;
import com.example.springboot.service.NoticeService;
import com.example.springboot.service.UserService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Autowired
    NoticeService noticeService;

    @Autowired
    UserService userService;

    @UserLogs(operation = "Notice", type = LogType.ADD)
    @PostMapping("/add")
    public Result add(@RequestBody Notice notice){
        User currentUser = TokenUtils.getCurrentUser();
        notice.setUid(currentUser.getUid());
        notice.setTime(DateUtil.now());
        noticeService.save(notice);
        return Result.success();
    }

    @UserLogs(operation = "Notice", type = LogType.UPDATE)
    @PutMapping("/update")
    public Result update(@RequestBody Notice notice){
        noticeService.updateById(notice);
        return Result.success();
    }

    @UserLogs(operation = "Notice", type = LogType.DELETE)
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        noticeService.removeById(id);
        return Result.success();
    }

    @UserLogs(operation = "Notice", type = LogType.BATCH_DELETE)
    @DeleteMapping("/delete/batch")
    public Result batchDelete(@RequestBody List<Integer> ids){
        noticeService.removeBatchByIds(ids);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Notice> noticeList = noticeService.list(new QueryWrapper<Notice>().orderByDesc("id"));
        return Result.success(noticeList);
    }

    /**
     * 查询用户公告
     * */
    @GetMapping("/selectUserData")
    public Result selectUserData() {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<Notice>().orderByDesc("id");
        queryWrapper.eq("open", 1); // 用户只能看到公开的公告
        List<Notice> noticeList = noticeService.list(queryWrapper);
        return Result.success(noticeList);
    }

    /**
    * pageNum: The number of current page
    * pageSize: The size of each page
    */
    @GetMapping("/selectByPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String title) {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<Notice>().orderByDesc("id");
        queryWrapper.like(StrUtil.isNotBlank(title),"title", title);
        Page<Notice> page = noticeService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Notice> records = page.getRecords();
        for (Notice record : records) {
            Integer uid = record.getUid();
            User user = userService.getById(uid);
            if (user != null) {
                record.setUser(user.getName());
            }
        }
        return Result.success(page);
    }
}
