package com.TicketManagementSystem.DamaiTicketing.Config;

import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTask;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonDelayQueueConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setDatabase(0);
        return Redisson.create(config);
    }

    /**
     * 目标阻塞队列Bean
     * 到期消息会从延迟队列转移到这里 供消费者取出
     */
    @Bean("ticketBlockingDeque")
    public RBlockingDeque<TicketTask> ticketBlockingDeque(RedissonClient redissonClient) {
        return redissonClient.getBlockingDeque("ORDER_CLOSE_QUEUE");
    }

    /**
     * 延迟队列Bean：必须创建并持有它 后台定时任务才会启动
     * 依赖目标队列 将消息延时投递到目标队列
     */
    @Bean("delayedTicketQueue")
    public RDelayedQueue<TicketTask> delayedTicketQueue(RedissonClient redissonClient,
                                                            @Qualifier("ticketBlockingDeque") RBlockingQueue<TicketTask> targetQueue) {
        return redissonClient.getDelayedQueue(targetQueue);
    }

}
