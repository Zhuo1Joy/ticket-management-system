package com.TicketManagementSystem.DamaiTicketing.Entity;

import lombok.Data;

@Data
public class Response {

    private int code;
    private String message;
    private Object data;

    // 响应成功
    public static Response success(int code, String message) {
        Response response = new Response();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }

    public static Response success(int code, String message, Object data) {
        Response response = new Response();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    // 响应失败
    public static Response error(int code, String message) {
        Response response = new Response();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }

    public static Response error(int code, String message, Object data) {
        Response response = new Response();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    // 这里是不是可以合并成功和失败的响应 因为传进去的参数都一样？

}
