package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.LogType;
import com.example.springboot.common.Result;
import com.example.springboot.common.UserLogs;
import com.example.springboot.entity.*;
import com.example.springboot.service.RoutesService;
import com.example.springboot.service.UserService;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RoutesController {

    @Resource
    private RoutesService routesService;

    @Resource
    UserService userService;

    @UserLogs(operation = "Routes", type = LogType.UPDATE)
    @PutMapping("/update")
    public Result update(@RequestBody Routes routes){
        routesService.updateById(routes);
        return Result.success();
    }

    @UserLogs(operation = "Routes", type = LogType.DELETE)
    @DeleteMapping("/delete/{routeId}")
    public Result delete(@PathVariable Integer routeId){
        routesService.removeById(routeId);
        return Result.success();
    }

    @UserLogs(operation = "Routes", type = LogType.BATCH_DELETE)
    @DeleteMapping("/delete/batch")
    public Result batchDelete(@RequestBody List<Integer> routeIds){
        routesService.removeBatchByIds(routeIds);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Routes> routesList = routesService.list(new QueryWrapper<Routes>().orderByDesc("route_id"));
        return Result.success(routesList);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        List<WayPoints> wayPoints = routesService.getWayPointsByRouteId(id);
        if (wayPoints == null || wayPoints.isEmpty()) {
            return Result.error();
        }
        return Result.success(wayPoints);
    }

    @GetMapping("/selectByPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String routeName) {
        QueryWrapper<Routes> queryWrapper = new QueryWrapper<Routes>().orderByDesc("route_id");
        queryWrapper.like(StrUtil.isNotBlank(routeName),"route_name", routeName);
        Page<Routes> page = routesService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Routes> records = page.getRecords();
        for (Routes record : records) {
            Integer uid = record.getUid();
            User user = userService.getById(uid);
            if (user != null) {
                record.setUser(user.getName());
            }
        }
        return Result.success(page);
    }

    @PostMapping("/add")
    public Result addRoute(@RequestBody RouteAndWaypointsRequest request) {
        routesService.addRouteWithWaypoints(request.getRoutes(), request.getWayPointsList());
        return Result.success();
    }

    @Data
    public static class RouteAndWaypointsRequest {
        private Routes routes;
        private List<WayPoints> wayPointsList;
    }
}
