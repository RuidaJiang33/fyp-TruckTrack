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
public class Truck {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String licensePlate;
    private String model;
    private String capacity;
    private String status;
    private Double currentLongitude;
    private Double currentLatitude;
}
