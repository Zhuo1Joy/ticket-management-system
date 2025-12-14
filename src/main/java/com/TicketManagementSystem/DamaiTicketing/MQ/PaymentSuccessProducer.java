package com.TicketManagementSystem.DamaiTicketing.MQ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentSuccessProducer {

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void sentPaymentSuccessMessage(PaymentSuccessMessage paymentSuccessMessage) {
        try {
            rabbitTemplate.convertAndSend("ticket.exchange", "payment.key", paymentSuccessMessage);

            log.info("支付成功消息已发送，订单号：{}", paymentSuccessMessage.getBusinessOrderNo());
        } catch (Exception e) {
            log.error("支付成功消息发送失败");
            throw new RuntimeException(e);
        }
    }

}
