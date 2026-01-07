package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.TicketManagementSystem.DamaiTicketing.Enums.RecordStatus;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private Long userId;
    private String subject;
    private String businessOrderNo;
    private String paymentOrderNo;
    private String tradeNo;
    private BigDecimal amount;
    private String qrCodeUrl;

    @Enumerated(EnumType.STRING) // 指定存储为字符串
    @Column(name = "status", length = 20, nullable = false) // 指定长度和非空
    private RecordStatus status;

    private LocalDateTime payTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}