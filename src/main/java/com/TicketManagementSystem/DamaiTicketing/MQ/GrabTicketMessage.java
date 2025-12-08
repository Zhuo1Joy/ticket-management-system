package com.TicketManagementSystem.DamaiTicketing.MQ;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GrabTicketMessage {

    private String requestId; // 请求ID
    private Long userId; // 用户ID
    private Long ticketId; // 票ID

    private Long performanceId; // 表演ID
    private Long sessionId; // 场次ID
    private Long tierId; // 票档ID
    private Integer quantity; // 买的票数量

}
