package com.example.springboot.exception;

import lombok.Getter;

/*
 * function:
 * author: Ruida
 * date: 2024/2/15 10:23
 */
@Getter
public class ServiceException extends RuntimeException{
    private final String code;
    public ServiceException(String msg) {
        super(msg);
        this.code = "500";
    }

    public ServiceException(String code, String msg) {
        super(msg);
        this.code = code;
    }

}
