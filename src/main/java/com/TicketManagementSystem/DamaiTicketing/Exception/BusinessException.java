package com.TicketManagementSystem.DamaiTicketing.Exception;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException {

    private int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
