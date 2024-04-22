package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Orders;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OrdersMapper extends BaseMapper<Orders> {

    @Select("SELECT * FROM orders WHERE tid = #{tid} LIMIT 1")
    Orders selectByTid(@Param("tid") Integer tid);
}
