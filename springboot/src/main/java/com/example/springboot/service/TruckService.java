package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.TruckMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TruckService extends ServiceImpl<TruckMapper, Truck> {

    private final TruckStatusLogService truckStatusLogService;

    @Autowired
    public TruckService(TruckStatusLogService truckStatusLogService) {
        this.truckStatusLogService = truckStatusLogService;
    }


    public Map<Integer, TruckStatus> calculateTruckStatuses() {
        List<TruckStatusLog> logs = truckStatusLogService.fetchTruckStatusLogs();
        Map<Integer, TruckStatus> statuses = new HashMap<>();
        TruckStatusLog prevLog = null; // 用于存储上一条日志记录

        for (TruckStatusLog log : logs) {
            // 确保每辆卡车的状态被初始化
            if (!statuses.containsKey(log.getTruckId())) {
                // 初始化时使用log.getLicensePlate()来设置TruckStatus的licensePlate字段
                statuses.put(log.getTruckId(), new TruckStatus(log.getLicensePlate(), 0L, 0L));
            }
            TruckStatus status = statuses.get(log.getTruckId());
            // 当处理同一辆卡车的连续记录时
            if (prevLog != null && prevLog.getTruckId().equals(log.getTruckId())) {
                long diff = Duration.between(prevLog.getTimestamp(), log.getTimestamp()).toMinutes();
                // 根据上一条记录的状态来决定如何计算时间差
                if ("Leisure".equals(prevLog.getStatus())) {
                    status.setLeisureTime(status.getLeisureTime() + diff);
                } else if ("On The Way".equals(prevLog.getStatus())) {
                    status.setServiceTime(status.getServiceTime() + diff);
                }
            }

            // 当处理的记录是新的卡车或者状态发生变化时，更新prevLog，准备下一次比较
            if (prevLog == null || !prevLog.getTruckId().equals(log.getTruckId()) || !prevLog.getStatus().equals(log.getStatus())) {
                prevLog = log;
            }
        }

        return statuses;
    }

    public List<Truck> findAllTrucks() {
        return list(); // 这里调用了ServiceImpl类中的list方法，该方法查询并返回所有记录
    }
}
