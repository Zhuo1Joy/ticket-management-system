package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("performance_session")
@Data
@NoArgsConstructor
public class PerformanceSession {

    @TableId
    private Long id;
    private Long performanceId; // 关联的演出ID
    private LocalDateTime sessionTime;
    private String venue;
    private String address;
    private Boolean isOnSale;
    private LocalDateTime createdTime;
    private LocalDateTime updateTime;

}
