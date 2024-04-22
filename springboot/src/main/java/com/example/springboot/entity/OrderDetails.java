package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/*
 * function:
 * author: Ruida
 * date: 2024/3/7 15:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetails {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer oid;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
