package com.TicketManagementSystem.DamaiTicketing.Enums;

import lombok.Getter;

@Getter
public enum RecordStatus {

    PENDING(1, "待支付"),
    PAID(2, "已支付"),
    CLOSED(3, "已关闭");

    private final int code;
    private final String description;

    RecordStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static RecordStatus fromCode(int code) {
        for (RecordStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的状态码: " + code);
    }

}
