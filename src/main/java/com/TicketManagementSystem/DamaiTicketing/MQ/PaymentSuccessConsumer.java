package com.TicketManagementSystem.DamaiTicketing.MQ;

import com.TicketManagementSystem.DamaiTicketing.Service.PayService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class PaymentSuccessConsumer {

    @Autowired
    PayService payService;

    @RabbitListener(queues = "payment.queue", ackMode = "MANUAL", concurrency = "5-10")
    public void handlePaymentSuccessMessage(PaymentSuccessMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        if (message.getBusinessOrderNo() == null || message.getTradeNo() == null) {
            log.error("❌ 消息字段不全: businessOrderNo={}, tradeNo={}", message.getBusinessOrderNo(), message.getPaymentOrderNo());
            return;
        }

        try {
            payService.processPaymentSuccess(message.getBusinessOrderNo(), message.getTradeNo(), message.getEmail());
            channel.basicAck(deliveryTag, false);
            log.info("支付消息处理成功，订单号：{}", message.getBusinessOrderNo());
        } catch (Exception e) {
            log.error("支付消息处理失败，订单号：{}", message.getBusinessOrderNo());
            throw e;
        }

        // TODO 处理失败情况未处理

    }

}
