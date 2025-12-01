package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private String celebrity;
    private Integer status;
    private LocalDateTime ticketStartTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

}
