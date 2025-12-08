package com.TicketManagementSystem.DamaiTicketing.Entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentResponse {

    private String orderNo;
    private String qrCodeUrl;
    private BigDecimal amount;

}
