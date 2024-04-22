package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Logs;
import com.example.springboot.entity.Notice;
import com.example.springboot.mapper.LogsMapper;
import com.example.springboot.mapper.NoticeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/*
 * function:
 * author: Ruida
 * date: 2024/2/14 10:09
 */
@Service

public class LogsService extends ServiceImpl<LogsMapper, Logs> {

}
