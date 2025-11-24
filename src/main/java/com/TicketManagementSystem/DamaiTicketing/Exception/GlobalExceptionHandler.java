package com.TicketManagementSystem.DamaiTicketing.Exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Response handleBusinessException (BusinessException b){
        return Response.error(b.getCode(), b.getMessage());
    }

    // 未登录异常
    @ExceptionHandler(NotLoginException.class)
    public Response handleNotLoginException(NotLoginException n) {
        return Response.error(401, NotLoginException.DEFAULT_MESSAGE);
        // 这里异常返回不够细节 后面来慢慢细分
    }

//    @ExceptionHandler(Exception.class) // 捕获所有异常
//    public Response ex(Exception ex) {
//        return Response.error(401, "对不起 操作失败 请联系管理员");
//    }

}
