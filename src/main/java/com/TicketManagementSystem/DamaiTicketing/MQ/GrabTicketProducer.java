package com.TicketManagementSystem.DamaiTicketing.MQ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GrabTicketProducer {

    @Autowired
    RabbitTemplate rabbitTemplate;

    // 发送抢票消息
    public void sendGrabTicketMessage(GrabTicketMessage message) {
        try {
            // 发送到队列
            rabbitTemplate.convertAndSend("ticket.exchange","ticket.key", message);

            log.info("抢票消息发送成功: requestId={}, userId={}, ticketId={}",
                    message.getRequestId(),
                    message.getUserId(),
                    message.getTicketId());
        } catch (Exception e) {
            log.error("抢票消息发送失败: {}", message, e);
            throw new RuntimeException("抢票请求发送失败 请稍后重试");
        }
    }

}
