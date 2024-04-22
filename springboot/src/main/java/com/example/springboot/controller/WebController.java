package com.example.springboot.controller;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.LogType;
import com.example.springboot.common.Result;
import com.example.springboot.common.UserLogs;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.TruckStatus;
import com.example.springboot.entity.TruckStatusLog;
import com.example.springboot.entity.User;
import com.example.springboot.service.OrdersService;
import com.example.springboot.service.TruckService;
import com.example.springboot.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/*
 * function:
 * author: Ruida
 * date: 2024/2/13 16:15
 */
@RestController
public class WebController {

    @Resource
    UserService userService;

    @Resource
    OrdersService ordersService;

    @Resource
    private TruckService truckService;

    @AuthAccess
    @GetMapping("/")
    public Result hello() { return Result.success("success");}

    @UserLogs(operation = "User", type = LogType.LOGIN)
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

    @UserLogs(operation = "User", type = LogType.REGISTER)
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
    @UserLogs(operation = "User", type = LogType.UPDATE)
    @AuthAccess
    @PutMapping("/password")
    public Result password(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPhone())) {
            return Result.error("invalid input");
        }
        userService.resetPassword(user);
        return Result.success();
    }

    /**
     *  获取统计数据
     *  @return 动态数据
     * */
    @GetMapping("/orderTrendCharts")
    public Result orderTrendCharts() {
        List<Orders> allOrders = ordersService.list(); // 获取所有订单
        Set<String> orderDates = allOrders.stream()
                .map(orders -> {
                    // 解析ZonedDateTime，然后转换为LocalDate
                    LocalDate date = ZonedDateTime.parse(orders.getOrderDate()).toLocalDate();
                    // 使用自定义格式化器将日期格式化为"yyyy-MM-dd"字符串
                    return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                })
                .collect(Collectors.toSet());

        List<Dict> trendList = new ArrayList<>();
        for (String orderDate : orderDates) {
            BigDecimal sum = allOrders.stream()
                    .filter(orders -> {
                        // 同样在过滤时需要转换日期格式
                        LocalDate date = ZonedDateTime.parse(orders.getOrderDate()).toLocalDate();
                        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).equals(orderDate);
                    })
                    .map(orders -> {
                        Orders orderWithDetails = ordersService.getOrderWithDetails(orders.getId());
                        return orderWithDetails.getOrderDetailsList().stream()
                                .map(orderDetails -> orderDetails.getPrice().multiply(BigDecimal.valueOf(orderDetails.getQuantity())))
                                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                    })
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

            Dict dict = Dict.create().set("orderDate", orderDate).set("value", sum);
            trendList.add(dict); // 将计算结果添加到趋势列表中
        }
        return Result.success(trendList);
    }

    @GetMapping("/truckStatusCharts")
    public Result truckStatusCharts() {
        // 使用TruckService的calculateTruckStatuses方法来获取计算后的卡车状态数据
        Map<Integer, TruckStatus> truckStatuses = truckService.calculateTruckStatuses();

        List<Dict> statusList = new ArrayList<>();
        for (TruckStatus truckStatus : truckStatuses.values()) {
            Dict dict = Dict.create()
                    .set("licensePlate", truckStatus.getLicensePlate()) // 卡车牌照
                    .set("leisureTime", truckStatus.getLeisureTime()) // 空闲时间
                    .set("serviceTime", truckStatus.getServiceTime()); // 运营时间
            statusList.add(dict);
        }

        // 返回结果
        return Result.success(statusList);
    }

    @GetMapping("/orderStatusCharts")
    public Result orderStatusCharts() {
        Map<String, Long> statusCounts = ordersService.countOrdersByStatus();

        List<Dict> statusDistribution = new ArrayList<>();
        statusCounts.forEach((status, count) -> {
            statusDistribution.add(Dict.create()
                    .set("status", status)
                    .set("count", count));
        });

        return Result.success(statusDistribution);
    }

}
