package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.baomidou.mybatisplus.annotation.*;
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
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime expireTime;
    private LocalDateTime paymentTime;
    private LocalDateTime cancelTime;
    private String cancelReason;

}