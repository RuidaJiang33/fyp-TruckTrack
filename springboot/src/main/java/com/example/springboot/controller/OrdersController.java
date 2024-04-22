package com.example.springboot.controller;

/*
 * function:
 * author: Ruida
 * date: 2024/2/14 10:11
 */

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.LogType;
import com.example.springboot.common.Result;
import com.example.springboot.common.UserLogs;
import com.example.springboot.entity.Notice;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.User;
import com.example.springboot.service.NoticeService;
import com.example.springboot.service.OrdersService;
import com.example.springboot.service.UserService;
import com.example.springboot.utils.TokenUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrdersController {
    @Autowired
    OrdersService ordersService;

    @Autowired
    UserService userService;

    @UserLogs(operation = "Orders", type = LogType.ADD)
    @PostMapping("/add")
    public Result add(@RequestBody Orders orders){
        User currentUser = TokenUtils.getCurrentUser();
        orders.setUid(currentUser.getUid());
        orders.setNo(IdUtil.fastSimpleUUID());
        ordersService.save(orders);
        return Result.success();
    }

    @UserLogs(operation = "Orders", type = LogType.UPDATE)
    @PutMapping("/update")
    public Result update(@RequestBody Orders orders){
        ordersService.updateById(orders);
        return Result.success();
    }

    @UserLogs(operation = "Orders", type = LogType.DELETE)
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        ordersService.removeById(id);
        return Result.success();
    }

    @UserLogs(operation = "Orders", type = LogType.BATCH_DELETE)
    @DeleteMapping("/delete/batch")
    public Result batchDelete(@RequestBody List<Integer> ids){
        ordersService.removeBatchByIds(ids);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Orders> ordersList = ordersService.list(new QueryWrapper<Orders>().orderByDesc("id"));
        return Result.success(ordersList);
    }

    @GetMapping("/selectById/{id}")
    public Result selectByUid(@PathVariable Integer id) {
        Orders orders = ordersService.getById(id);
        User user = userService.getById(orders.getUid());
        if (user != null) {
            orders.setUser(user.getName());
        }
        return Result.success(orders);
    }

    /**
    * pageNum: The number of current page
    * pageSize: The size of each page
    */
    @GetMapping("/selectByPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String no,
                               @RequestParam String status) {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<Orders>().orderByDesc("id");
        queryWrapper.like(StrUtil.isNotBlank(no),"no", no);
        queryWrapper.like(StrUtil.isNotBlank(status),"status", status);
        Page<Orders> page = ordersService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Orders> records = page.getRecords();
        for (Orders record : records) {
            Integer uid = record.getUid();
            User user = userService.getById(uid);
            if (user != null) {
                record.setUser(user.getName());
            }
        }
        return Result.success(page);
    }


    /**
     * 批量导出数据
     * */
    @GetMapping("/export")
    public void exportData(@RequestParam(required = false) String no,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String ids,
                           HttpServletResponse response) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        List<Orders> list;
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(ids)){
            List<Integer> idsArr = Arrays.stream(ids.split(",")).map(Integer::valueOf).collect(Collectors.toList());
            queryWrapper.in("id", idsArr);
        } else {
            // 全部导出，或条件导出
            queryWrapper.like(StrUtil.isNotBlank(no),"no", no);
            queryWrapper.like(StrUtil.isNotBlank(status),"status", status);
        }
        list = ordersService.list(queryWrapper); // 查询出当前User表的所有数据
        writer.write(list, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Dispositon", "attachment;filename=" + URLEncoder.encode("Orders info", "UTF-8") + ".xlsx");
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
        List<Orders> ordersList = reader.readAll(Orders.class); // 确保这里正确映射到您的实体类

        try {
            ordersService.saveOrUpdateBatch(ordersList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("File import error: " + e.getMessage());
        }
        return Result.success("Data imported successfully.");
    }
}
