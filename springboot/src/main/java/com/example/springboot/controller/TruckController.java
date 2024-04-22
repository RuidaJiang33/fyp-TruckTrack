package com.example.springboot.controller;

/*
 * function:
 * author: Ruida
 * date: 2024/2/14 10:11
 */

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.LogType;
import com.example.springboot.common.Result;
import com.example.springboot.common.UserLogs;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.Truck;
import com.example.springboot.service.OrdersService;
import com.example.springboot.service.TruckService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/truck")
public class TruckController {
    @Resource
    TruckService truckService;

    @Resource
    OrdersService ordersService;

    @UserLogs(operation = "Truck", type = LogType.ADD)
    @PostMapping("/add")
    public Result add(@RequestBody Truck truck){
        truckService.save(truck);
        return Result.success();
    }

    @UserLogs(operation = "Truck", type = LogType.UPDATE)
    @PutMapping("/update")
    public Result update(@RequestBody Truck truck){
        truckService.updateById(truck);
        return Result.success();
    }

    @UserLogs(operation = "Truck", type = LogType.DELETE)
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        truckService.removeById(id);
        return Result.success();
    }

    @UserLogs(operation = "Truck", type = LogType.BATCH_DELETE)
    @DeleteMapping("/delete/batch")
    public Result batchDelete(@RequestBody List<Integer> ids){
        truckService.removeBatchByIds(ids);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Truck> truckList = truckService.list(new QueryWrapper<Truck>().orderByDesc("id"));
        return Result.success(truckList);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Truck truck = truckService.getById(id);
        return Result.success(truck);
    }

    /**
    * pageNum: The number of current page
    * pageSize: The size of each page
    */
    @GetMapping("/selectByPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String licensePlate,
                               @RequestParam String status) {
        QueryWrapper<Truck> queryWrapper = new QueryWrapper<Truck>().orderByDesc("id");
        queryWrapper.like(StrUtil.isNotBlank(licensePlate),"license_plate", licensePlate);
        queryWrapper.like(StrUtil.isNotBlank(status),"status", status);
        Page<Truck> page = truckService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }

    /**
     * 批量导出数据
     * */
    @GetMapping("/export")
    public void exportData(@RequestParam(required = false) String licensePlate,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String ids,
                           HttpServletResponse response) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        List<Truck> list;
        QueryWrapper<Truck> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(ids)){
            List<Integer> idsArr = Arrays.stream(ids.split(",")).map(Integer::valueOf).collect(Collectors.toList());
            queryWrapper.in("id", idsArr);
        } else {
            // 全部导出，或条件导出
            queryWrapper.like(StrUtil.isNotBlank(licensePlate),"licensePlate", licensePlate);
            queryWrapper.like(StrUtil.isNotBlank(status),"status", status);
        }
        list = truckService.list(queryWrapper); // 查询出当前User表的所有数据
        writer.write(list, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Dispositon", "attachment;filename=" + URLEncoder.encode("Truck info", "UTF-8") + ".xlsx");
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
        List<Truck> truckList = reader.readAll(Truck.class); // 确保这里正确映射到您的实体类

        try {
            truckService.saveOrUpdateBatch(truckList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("File import error: " + e.getMessage());
        }
        return Result.success("Data imported successfully.");
    }

    @GetMapping("/fetchTruckLocations")
    public Result fetchTruckLocations() {
        // 获取所有卡车的当前位置信息
        List<Truck> trucks = truckService.findAllTrucks();

        // 仅为非空闲状态的卡车找到对应的订单
        List<Dict> truckLocations = trucks.stream()
                .filter(truck -> !"Leisure".equals(truck.getStatus())) // 过滤掉状态为idle的卡车
                .map(truck -> {
                    Orders order = ordersService.findOrderByTruckId(truck.getId()); // 根据卡车ID找到订单
                    Dict truckLocation = new Dict();
                    truckLocation.put("id", truck.getId());
                    truckLocation.put("lng", truck.getCurrentLongitude());
                    truckLocation.put("lat", truck.getCurrentLatitude());
                    truckLocation.put("licensePlate", truck.getLicensePlate());
                    if (order != null) {
                        truckLocation.put("origin", order.getOrigin());
                        truckLocation.put("destination", order.getDestination());
                        // 假设getCoordsByLocation方法已实现，用于获取起点和终点的坐标
                        truckLocation.put("originCoords", ordersService.getCoordsByLocation(order.getOrigin()));
                        truckLocation.put("destinationCoords", ordersService.getCoordsByLocation(order.getDestination()));
                    }
                    return truckLocation;
                })
                .collect(Collectors.toList());

        return Result.success(truckLocations);
    }

}
