package com.TicketManagementSystem.DamaiTicketing.MQ;

import com.TicketManagementSystem.DamaiTicketing.Entity.GrabTicketRequest;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Service.TicketGrabbingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import com.rabbitmq.client.Channel;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GrabTicketConsumer {

    @Autowired
    private TicketGrabbingService ticketGrabbingService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String GRAB_RESULT = "grab_result:";

    @RabbitListener(queues = "ticket.queue", ackMode = "MANUAL", concurrency = "5-10") // 动态并发范围 最少5个 最多10个 根据负载动态调整
    public void handleGrabTicketMessage(GrabTicketMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("开始处理抢票请求: requestId={}, userId={}, ticketId={}",
                message.getRequestId(),
                message.getUserId(),
                message.getTicketId());


        // 检查是否是无效消息
        if (message.getUserId() == null || message.getTicketId() == null) {
            log.error("❌ 消息字段不全: userId={}, ticketId={}", message.getUserId(), message.getTicketId());
            return;
        }

        String resultKey = GRAB_RESULT + message.getRequestId();
        GrabTicketRequest grabTicketRequest = convertToRequest(message);

        try {

            boolean success = ticketGrabbingService.grabTicket(grabTicketRequest, message.getUserId());
            if (success) {
                redisTemplate.opsForValue().set(resultKey, "Processed", 30, TimeUnit.MINUTES);
            }

            channel.basicAck(deliveryTag, false);

            log.info("抢票处理完成: requestId={}", message.getRequestId());

        } catch (BusinessException b) {
            // 不管成不成功我都直接删去 不做批量处理
            channel.basicAck(deliveryTag, false);
            redisTemplate.opsForValue().set(resultKey, "Processing failed", 30, TimeUnit.MINUTES);
            // 抛出业务异常
            throw b;
        } catch (Exception e) {
            // 进入死信队列
            channel.basicNack(deliveryTag, false, false);
            log.error("💥 处理抢票请求异常: requestId={}, 消息进入死信队列:", message.getRequestId(), e);
            redisTemplate.opsForValue().set(resultKey, "Processing failed", 30, TimeUnit.MINUTES);
            throw e;
        }

    }

    public GrabTicketRequest convertToRequest(GrabTicketMessage grabTicketMessage) {

        GrabTicketRequest grabTicketRequest = new GrabTicketRequest();
        grabTicketRequest.setPerformanceId(grabTicketMessage.getPerformanceId());
        grabTicketRequest.setSessionId(grabTicketMessage.getSessionId());
        grabTicketRequest.setTierId(grabTicketMessage.getTierId());
        grabTicketRequest.setQuantity(grabTicketMessage.getQuantity());

        return grabTicketRequest;

    }

}
