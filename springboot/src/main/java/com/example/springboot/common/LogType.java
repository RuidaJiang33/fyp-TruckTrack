package com.example.springboot.common;

/**
 * 系统日志的操作类型枚举
 * */
public enum LogType {
    ADD("ADD"),UPDATE("UPDATE"),DELETE("DELETE"),BATCH_DELETE("BATCH_DELETE"),LOGIN("LOGIN"),REGISTER("REGISTER");

    private String value;

    public String getValue() {
        return value;
    }

    LogType(String value) {
        this.value = value;
    }
}
