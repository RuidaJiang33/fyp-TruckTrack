package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.WayPoints;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WayPointsMapper extends BaseMapper<WayPoints> {

    @Select("SELECT * FROM way_points WHERE route_id = #{route_id}")
    List<WayPoints> findByRouteId(@Param("route_id") Integer routeId);
}
