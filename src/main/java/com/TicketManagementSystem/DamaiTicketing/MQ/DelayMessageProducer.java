package com.TicketManagementSystem.DamaiTicketing.MQ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelayMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 发送延迟消息
    public void sendDelayMessage(String paymentOrderNo, int delayMinutes) {
        rabbitTemplate.convertAndSend(
                "order.delayed.exchange",
                "order.delay.key",
                paymentOrderNo,
                message -> {
                    // 关键：设置延迟时间（毫秒）
                    message.getMessageProperties()
                            .setHeader("x-delay", delayMinutes * 60 * 1000);
                    return message;
                }
        );
        log.info("延迟消息已发送: 交易订单编号{}, 延迟{}分钟", paymentOrderNo, delayMinutes);
    }

}