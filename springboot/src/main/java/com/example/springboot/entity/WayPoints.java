package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * function:
 * author: Ruida
 * date: 2024/3/7 17:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WayPoints {
    @TableId(type = IdType.AUTO)
    private Integer wayPointId;
    private Integer routeId;
    private double latitude;
    private double longitude;
    private Integer sequence;
}
