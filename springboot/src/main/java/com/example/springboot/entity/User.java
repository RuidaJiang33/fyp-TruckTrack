package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/*
 * function:
 * author: Ruida
 * date: 2024/2/14 10:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("user")
public class User {
    @TableId(type=IdType.AUTO)
    private Integer uid;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String role;

    @TableField(exist = false)
    private String token;
}
