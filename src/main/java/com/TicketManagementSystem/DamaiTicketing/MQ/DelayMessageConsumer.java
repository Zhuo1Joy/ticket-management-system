package com.TicketManagementSystem.DamaiTicketing.MQ;

import com.TicketManagementSystem.DamaiTicketing.Service.AlipayService;
import com.TicketManagementSystem.DamaiTicketing.Service.PayService;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class DelayMessageConsumer {

    @Autowired
    AlipayService alipayService;

    @Autowired
    PayService payService;

    @RabbitListener(queues = "order.delay.queue")
    public void handleDelayMessage(String paymentOrderNo, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("收到延迟消息，开始处理: {}", paymentOrderNo);

            // 1. 检查订单状态（防止重复处理）
            if (isOrderProcessed(paymentOrderNo)) {
                log.info("该订单已成功支付，跳过: {}", paymentOrderNo);
                channel.basicAck(deliveryTag, false);
                return;
            }
            // 2. 执行业务逻辑（比如关闭订单）
            boolean result = closeTimeOutOrder(paymentOrderNo);

            // 3. 确认消息（成功才确认）
            if (result) {
                channel.basicAck(deliveryTag, false);
                log.info("超时订单处理成功，相关订单已关闭: {}", paymentOrderNo);
            } else {
                // 处理失败（不重回队列）
                channel.basicNack(deliveryTag, false, false);
                log.warn("超时订单处理失败: {}", paymentOrderNo);
            }

        } catch (Exception e) {
            log.error("处理延迟消息异常: 支付订单{}", paymentOrderNo, e);
            // 异常时拒绝消息（不重回队列）
            channel.basicNack(deliveryTag, false, false);
        }
    }

    public boolean isOrderProcessed(String paymentOrderNo) {
        // 检查订单支付状态
        try {
            AlipayTradeQueryResponse response = alipayService.queryAlipayOrderStatus(paymentOrderNo);
            return response.getTradeStatus().equals("TRADE_SUCCESS") || response.getTradeStatus().equals("TRADE_FINISHED");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean closeTimeOutOrder(String paymentOrderNo) {
        // 关闭订单
        try {
            payService.closeTimeOutOrder(paymentOrderNo);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
