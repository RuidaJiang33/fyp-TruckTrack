package com.example.springboot.common;

import lombok.*;

/*
 * function:
 * author: Ruida
 * date: 2024/2/13 16:20
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result {

    public static final String CODE_SUCCESS = "200";
    public static final String CODE_AUTH_ERROR = "401";
    public static final String CODE_SYS_ERROR = "500";

    private String code;
    private String msg;
    private Object data;

    public static Result success(){
        return Result.builder().code(CODE_SUCCESS).msg("success").build();
    }

    public static Result success(Object data){
        return new Result(CODE_SUCCESS, "success", data);
    }

    public static Result success(String msg){
        return new Result(CODE_SUCCESS, msg, null);
    }

    public static Result error(){
        return new Result(CODE_SYS_ERROR, "system error", null);
    }

    public static Result error(String msg){
        return new Result(CODE_AUTH_ERROR, msg, null);
    }
    public static Result error(String code, String msg){
        return new Result(code, msg, null);
    }

}
