package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("ticket_tier")
@Data
@NoArgsConstructor
public class TicketTier {

    @TableId
    private Long id;
    private Long sessionId; // 关联的场次ID
    private String tierName;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Boolean isAvailable;
    @Version
    private Integer version;
    private LocalDateTime createdTime;
    private LocalDateTime updateTime;

}
