package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Routes;
import com.example.springboot.entity.WayPoints;
import com.example.springboot.mapper.RoutesMapper;
import com.example.springboot.mapper.WayPointsMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoutesService extends ServiceImpl<RoutesMapper, Routes> {

    @Resource
    private RoutesMapper routesMapper;

    @Resource
    private WayPointsMapper wayPointsMapper;

    // 添加路线及其路线点
    public void addRouteWithWaypoints(Routes routes, List<WayPoints> wayPoints) {
        routesMapper.insert(routes); // 插入路线获取RouteID
        Integer routeId = routes.getRouteId(); // 假设insert操作后route对象的routeId已更新
        wayPoints.forEach(waypoint -> {
            waypoint.setRouteId(routeId); // 为每个路线点设置RouteID
            wayPointsMapper.insert(waypoint); // 插入路线点
        });
    }

    public List<WayPoints> getWayPointsByRouteId(Integer routeId) {
        return wayPointsMapper.findByRouteId(routeId);
    }
}
