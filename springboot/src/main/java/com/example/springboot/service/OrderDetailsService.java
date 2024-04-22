package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.OrderDetails;
import com.example.springboot.mapper.OrderDetailsMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailsService extends ServiceImpl<OrderDetailsMapper, OrderDetails> {

}
