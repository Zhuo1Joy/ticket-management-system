package com.TicketManagementSystem.DamaiTicketing.Enums;

import lombok.Getter;

@Getter
public enum OrderStatus {

    WITHHOLD(1, "预扣"),
    PAID(2, "已支付"),
    CANCELED(3, "已取消");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的状态码: " + code);
    }

}
