package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/*
 * function:
 * author: Ruida
 * date: 2024/3/5 19:58
 */
@Data
public class Notice {
    @TableId(type= IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    private String time;
    private Integer uid;
    private Boolean open;

    @TableField(exist = false)
    private String user;
}
