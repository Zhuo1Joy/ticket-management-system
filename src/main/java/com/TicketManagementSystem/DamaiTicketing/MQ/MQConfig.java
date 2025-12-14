package com.TicketManagementSystem.DamaiTicketing.MQ;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 创建抢票队列
    @Bean
    public Queue ticketQueue() {
        return QueueBuilder.durable("ticket.queue")  // 队列名称：ticket.queue + 持久化
                .withArgument("x-message-ttl", 30000) // 设置消息过期时间为30秒
                .withArgument("x-max-length", 1000) // 设置队列最大长度为1000
                .withArgument("x-dead-letter-exchange", "dlx.exchange")  // 死信交换机名称
                .withArgument("x-dead-letter-routing-key", "dlx.key")    // 死信路由键
                .build();  // 构建队列对象
    }

    // 创建支付成功队列
    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable("payment.queue")
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-max-length", 1000)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "dlx.key")
                .build();
    }

    // 创建死信队列
    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable("dlx.queue")  // 死信队列名称 持久化
                .withArgument("x-message-ttl", 7 * 24 * 60 * 60 * 1000) // 7天后过期
                .build();  // 构建队列对象
    }

    // 创建正常交换机
    @Bean
    public DirectExchange ticketExchange() {
        return new DirectExchange("ticket.exchange");
    }

    // 创建死信交换机
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange("dlx.exchange");
    }

    // 绑定正常队列到正常交换机
    @Bean
    public Binding ticketBinding() {
        return new Binding("ticket.queue",
                Binding.DestinationType.QUEUE,
                "ticket.exchange",
                "ticket.key",
                null);
    }

    // 绑定支付队列到正常交换机
    @Bean
    public Binding paymentBinding() {
        return new Binding("payment.queue",
                Binding.DestinationType.QUEUE,
                "ticket.exchange",
                "payment.key",
                null);
    }

    // 绑定死信队列到死信交换机
    @Bean
    public Binding dlxBinding() {
        return new Binding("dlx.queue",
                Binding.DestinationType.QUEUE,
                "dlx.exchange",
                "dlx.key",
                null);
    }

}
