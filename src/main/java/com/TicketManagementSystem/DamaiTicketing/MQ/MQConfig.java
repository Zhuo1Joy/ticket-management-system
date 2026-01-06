package com.TicketManagementSystem.DamaiTicketing.MQ;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

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

    // 创建延迟队列
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable("order.delay.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")  // 死信交换机
                .withArgument("x-dead-letter-routing-key", "dlx.key")    // 死信路由键
                .withArgument("x-message-ttl", 10000)  // 消息存活时间10秒
                .build();
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

    // 创建延迟交换机（必须用CustomExchange）
    @Bean
    public CustomExchange delayedExchange() {
        return new CustomExchange(
                "order.delayed.exchange",    // 交换机名
                "x-delayed-message",         // 固定类型
                true,                       // 持久化
                false,                      // 不自动删除
                Map.of("x-delayed-type", "direct")  // 参数
        );
    }

    // 绑定抢票队列到正常交换机
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

    // 绑定延迟队列到延迟交换机
    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(delayedExchange())
                .with("order.delay.key")
                .noargs();
    }

}
