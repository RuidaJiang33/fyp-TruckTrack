package com.example.springboot.controller;

/*
 * function:
 * author: Ruida
 * date: 2024/2/14 10:11
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.LogType;
import com.example.springboot.common.Result;
import com.example.springboot.common.UserLogs;
import com.example.springboot.entity.OrderDetails;
import com.example.springboot.entity.Truck;
import com.example.springboot.service.OrderDetailsService;
import com.example.springboot.service.TruckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orderDetails")
public class OrderDetailsController {
    @Autowired
    OrderDetailsService orderDetailsService;

    @UserLogs(operation = "OrderDetails", type = LogType.ADD)
    @PostMapping("/add")
    public Result add(@RequestBody OrderDetails orderDetails){
        orderDetailsService.save(orderDetails);
        return Result.success();
    }

    @UserLogs(operation = "OrderDetails", type = LogType.UPDATE)
    @PutMapping("/update")
    public Result update(@RequestBody OrderDetails orderDetails){
        orderDetailsService.updateById(orderDetails);
        return Result.success();
    }

    @UserLogs(operation = "OrderDetails", type = LogType.DELETE)
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        orderDetailsService.removeById(id);
        return Result.success();
    }

    @UserLogs(operation = "OrderDetails", type = LogType.BATCH_DELETE)
    @DeleteMapping("/delete/batch")
    public Result batchDelete(@RequestBody List<Integer> ids){
        orderDetailsService.removeBatchByIds(ids);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<OrderDetails> orderDetailsList = orderDetailsService.list(new QueryWrapper<OrderDetails>().orderByDesc("id"));
        return Result.success(orderDetailsList);
    }

    @GetMapping("/selectById/{id}")
    public Result selectByUid(@PathVariable Integer id) {
        OrderDetails orderDetails = orderDetailsService.getById(id);
        return Result.success(orderDetails);
    }

    /**
    * pageNum: The number of current page
    * pageSize: The size of each page
    */
    @GetMapping("/selectByOrderId/{oid}")
    public Result selectByOrderId(@PathVariable Integer oid,
                                  @RequestParam Integer pageNum,
                                  @RequestParam Integer pageSize) {
        Page<OrderDetails> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderDetails> queryWrapper = new QueryWrapper<OrderDetails>().eq("oid", oid).orderByDesc("id");
        Page<OrderDetails> orderDetailsPage = orderDetailsService.page(page, queryWrapper);
        return Result.success(orderDetailsPage);
    }
}
