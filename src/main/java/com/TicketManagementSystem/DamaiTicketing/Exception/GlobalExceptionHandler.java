package com.TicketManagementSystem.DamaiTicketing.Exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
        // TODO 这里异常返回不是很细节 后面来慢慢细分
    }

    // 运行时异常
    @ExceptionHandler(RuntimeException.class)
    public Response handleRuntimeException(RuntimeException r) {
        return Response.error(401, r.getMessage());
    }

    // 校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response handleValidationException(MethodArgumentNotValidException e) {
        // 这里最好不这么写 以后大概不止抢票数量不对的问题 但是现在我先这么写（
        return Response.error(400, "单个账号一次最多可抢两张票 最少需购一张票");
    }

    // 捕获所有异常
    @ExceptionHandler(Exception.class)
    public Response ex(Exception ex) {
        return Response.error(401, "对不起 操作失败 请联系管理员", ex.getMessage());
    }

}
