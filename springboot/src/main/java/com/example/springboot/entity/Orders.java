package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/*
 * function:
 * author: Ruida
 * date: 2024/3/7 15:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Orders {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String no;
    private Integer uid;
    private Integer tid;
    private String origin;
    private String destination;
    private String orderDate;
    private String deliveryDate;
    private String status;

    @TableField(exist = false)
    private List<OrderDetails> orderDetailsList = new ArrayList<>();

    @TableField(exist = false)
    private String user;
}
