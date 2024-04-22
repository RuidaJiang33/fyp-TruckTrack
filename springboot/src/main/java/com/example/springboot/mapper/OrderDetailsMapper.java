package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.OrderDetails;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderDetailsMapper extends BaseMapper<OrderDetails> {
    @Select("SELECT * FROM order_details WHERE oid = #{oid}")
    List<OrderDetails> selectByOid(@Param("oid") Integer oid);
}
