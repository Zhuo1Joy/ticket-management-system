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

    // 用户邮箱 用于支付成功后通知
    private String email;

}
