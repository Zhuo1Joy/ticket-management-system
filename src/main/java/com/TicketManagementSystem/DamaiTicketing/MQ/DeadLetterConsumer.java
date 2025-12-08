package com.TicketManagementSystem.DamaiTicketing.MQ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterConsumer {

    @RabbitListener(queues = "dlx.queue")
    public void handleDeadLetter(String message) {
        // 处理失败消息
        log.error("失败消息: {}", message);
    }

}
