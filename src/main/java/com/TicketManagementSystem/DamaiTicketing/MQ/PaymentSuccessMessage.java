package com.TicketManagementSystem.DamaiTicketing.MQ;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentSuccessMessage {

    private String requestId;
    private String businessOrderNo;
    private String paymentOrderNo;
    private String tradeNo;

    // 用于发送邮箱通知
    private String email;

}
