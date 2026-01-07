package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.TicketManagementSystem.DamaiTicketing.Enums.OrderStatus;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ticket_order")
public class TicketOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String orderNo;
    private String orderName;
    private Long userId;
    private String userEmail;
    private Long performanceId;
    private Long sessionId;
    private Long tierId;
    private Integer quantity;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING) // 指定存储为字符串
    @Column(name = "status", length = 20, nullable = false) // 指定长度和非空
    private OrderStatus status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime expireTime;
    private LocalDateTime paymentTime;
    private LocalDateTime cancelTime;
    private String cancelReason;

}