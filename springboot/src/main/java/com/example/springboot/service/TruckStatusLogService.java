package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Truck;
import com.example.springboot.entity.TruckStatusLog;
import com.example.springboot.mapper.TruckStatusLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * function:
 * author: Ruida
 * date: 2024/2/14 10:09
 */
@Service
public class TruckStatusLogService extends ServiceImpl<TruckStatusLogMapper, TruckStatusLog> {

    public List<TruckStatusLog> fetchTruckStatusLogs() {
        QueryWrapper<TruckStatusLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("truck_id", "timestamp"); // 确保按照truck_id和timestamp排序
        return list(queryWrapper);
    }

}
