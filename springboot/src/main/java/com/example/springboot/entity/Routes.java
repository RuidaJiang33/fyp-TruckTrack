package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * function:
 * author: Ruida
 * date: 2024/3/7 15:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Routes {
    @TableId(type = IdType.AUTO)
    private Integer routeId;
    private String routeName;
    private LocalDateTime createAt;
    private Integer uid;

    @TableField(exist = false)
    private String user;
}
