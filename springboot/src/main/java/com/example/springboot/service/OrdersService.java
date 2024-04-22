package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.OrderDetails;
import com.example.springboot.entity.Orders;
import com.example.springboot.mapper.OrderDetailsMapper;
import com.example.springboot.mapper.OrdersMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrdersService extends ServiceImpl<OrdersMapper, Orders> {
    @Resource
    private OrderDetailsMapper orderDetailsMapper;

    @Resource
    private OrdersMapper ordersMapper;

    public Orders getOrderWithDetails(Integer oid) {
        // 使用MyBatis-Plus提供的方法根据ID查询订单
        Orders order = this.getById(oid);
        if (order != null) {
            // 根据订单ID查询相关的所有订单详情，并设置到订单实体中
            List<OrderDetails> detailsList = orderDetailsMapper.selectByOid(order.getId());
            order.setOrderDetailsList(detailsList);
        }
        return order;
    }

    public Map<String, Long> countOrdersByStatus() {
        // 查询所有订单
        List<Orders> orders = ordersMapper.selectList(new QueryWrapper<>());
        // 按状态分组计数
        return orders.stream().collect(Collectors.groupingBy(Orders::getStatus, Collectors.counting()));
    }

    public Double[] getCoordsByLocation(String location) {
        switch (location) {
            case "Waterford":
                return new Double[]{-7.117901, 52.256852};
            case "Dublin":
                return new Double[]{-6.366551, 53.354294};
            default:
                return new Double[]{0.0, 0.0}; // 默认值或抛出异常
        }
    }

    public Orders findOrderByTruckId(Integer tid) {
        // 直接调用Mapper的方法根据卡车ID查询订单
        Orders order = ordersMapper.selectByTid(tid);
        if (order != null) {
            // 可以在这里调用其他方法填充订单的更多细节，比如订单详情
            // 假设有一个方法来获取订单详情列表并设置到订单实体中
            List<OrderDetails> detailsList = orderDetailsMapper.selectByOid(order.getId());
            order.setOrderDetailsList(detailsList);
        }
        return order;
    }
}
