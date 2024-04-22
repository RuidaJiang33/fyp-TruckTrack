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
import com.example.springboot.entity.TruckStatusLog;
import com.example.springboot.service.TruckService;
import com.example.springboot.service.TruckStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/truckStatusLog")
public class TruckStatusLogController {
    @Autowired
    TruckStatusLogService truckStatusLogService;

    @UserLogs(operation = "TruckStatusLog", type = LogType.ADD)
    @PostMapping("/add")
    public Result add(@RequestBody TruckStatusLog truckStatusLog){
        truckStatusLogService.save(truckStatusLog);
        return Result.success();
    }

    @UserLogs(operation = "TruckStatusLog", type = LogType.UPDATE)
    @PutMapping("/update")
    public Result update(@RequestBody TruckStatusLog truckStatusLog){
        truckStatusLogService.updateById(truckStatusLog);
        return Result.success();
    }

    @UserLogs(operation = "TruckStatusLog", type = LogType.DELETE)
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        truckStatusLogService.removeById(id);
        return Result.success();
    }

    @UserLogs(operation = "TruckStatusLog", type = LogType.BATCH_DELETE)
    @DeleteMapping("/delete/batch")
    public Result batchDelete(@RequestBody List<Integer> ids){
        truckStatusLogService.removeBatchByIds(ids);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<TruckStatusLog> truckStatusLogList = truckStatusLogService.list(new QueryWrapper<TruckStatusLog>().orderByDesc("id"));
        return Result.success(truckStatusLogList);
    }

    /**
    * pageNum: The number of current page
    * pageSize: The size of each page
    */
    @GetMapping("/selectByTruckId/{truckId}")
    public Result selectByOrderId(@PathVariable Integer truckId,
                                  @RequestParam Integer pageNum,
                                  @RequestParam Integer pageSize) {
        Page<TruckStatusLog> page = new Page<>(pageNum, pageSize);
        QueryWrapper<TruckStatusLog> queryWrapper = new QueryWrapper<TruckStatusLog>().eq("truck_id", truckId).orderByDesc("id");
        Page<TruckStatusLog> truckStatusLogPage = truckStatusLogService.page(page, queryWrapper);
        return Result.success(truckStatusLogPage);
    }
}
