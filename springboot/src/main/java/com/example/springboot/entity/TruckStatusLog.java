package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/*
 * function:
 * author: Ruida
 * date: 2024/3/7 17:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckStatusLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer truckId;
    private String licensePlate;
    private String status;
    private LocalDateTime timestamp;
}
