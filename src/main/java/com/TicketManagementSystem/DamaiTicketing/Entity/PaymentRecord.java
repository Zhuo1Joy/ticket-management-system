package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@TableName("payment_record")
public class PaymentRecord {

    @TableId
    private Long id;
    private String businessOrderNo;
    private String paymentOrderNo;
    private String tradeNo;
    private BigDecimal amount;
    private Integer status;
    private Long userId;
    private String qrCodeUrl;
    private String subject;
    private LocalDateTime payTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}