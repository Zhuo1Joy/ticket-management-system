package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.TicketManagementSystem.DamaiTicketing.Enums.PerformanceCategory;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("performance")
@Data
@NoArgsConstructor
public class Performance {

    @TableId
    private Long id;
    private String title;
    private String city;
    private String category;

    @Enumerated(EnumType.STRING) // 指定存储为字符串
    @Column(name = "status", length = 20, nullable = false) // 指定长度和非空
    private PerformanceCategory celebrity;

    private Integer status;
    private LocalDateTime ticketStartTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

}
