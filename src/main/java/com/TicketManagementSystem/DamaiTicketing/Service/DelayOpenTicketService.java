package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTask;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonShutdownException;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// 感谢人工智能赞助之
@Service
@Slf4j
public class DelayOpenTicketService {

    private final AutoStartTicketService autoStartTicketService;
    private final RBlockingDeque<TicketTask> ticketBlockingDeque;
    private final RDelayedQueue<TicketTask> delayedTicketQueue;

    private volatile boolean running = true;
    private Thread consumerThread;

    public DelayOpenTicketService(AutoStartTicketService autoStartTicketService,
                                  @Qualifier("ticketBlockingDeque") RBlockingDeque<TicketTask> ticketBlockingDeque,
                                  @Qualifier("delayedTicketQueue") RDelayedQueue<TicketTask> delayedTicketQueue) {
        this.autoStartTicketService = autoStartTicketService;
        this.ticketBlockingDeque = ticketBlockingDeque;
        this.delayedTicketQueue = delayedTicketQueue;
    }

    @PostConstruct
    public void startTicketTask() {
        log.info("启动延迟票务队列消费者...");

        consumerThread = new Thread(() -> {
            Thread.currentThread().setName("ticket-delay-consumer");

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    TicketTask task = ticketBlockingDeque.poll(1, TimeUnit.SECONDS);

                    if (task != null) {
                        processTask(task);
                    }

                } catch (RedissonShutdownException e) {
                    log.warn("Redisson已关闭 消费者线程退出");
                    break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("消费者线程被中断");
                    break;
                } catch (Exception e) {
                    log.error("处理延迟任务异常", e);
                }
            }

            log.info("延迟票务队列消费者停止");
        });

        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    private void processTask(TicketTask task) {
        if (task == null || task.getTaskType() == null) {
            log.error("任务或任务类型为空");
            return;
        }

        String taskType = task.getTaskType();

        try {
            if ("INIT_STOCK".equals(taskType)) {
                log.info("执行库存初始化 演出ID: {}", task.getTaskId());
                autoStartTicketService.preloadStockBeforeOpening(task.getTaskId());
            } else if ("OPEN_TICKET".equals(taskType)) {
                log.info("执行开票任务 演出ID: {}", task.getTaskId());
                autoStartTicketService.openTicket(task.getTaskId());
            } else {
                log.warn("未知任务类型: {}", taskType);
            }
        } catch (Exception e) {
            log.error("执行任务失败: {}", taskType, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("开始关闭延迟票务服务...");

        // 1. 停止消费者线程
        running = false;

        if (consumerThread != null && consumerThread.isAlive()) {
            consumerThread.interrupt();

            try {
                consumerThread.join(5000);
                log.info("消费者线程已停止");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待消费者线程停止时被中断");
            }
        }

        // 2. 销毁延迟队列（重要！）
        destroyDelayedQueue();

        log.info("✅ 延迟票务服务关闭完成");
    }

    /**
     * 销毁延迟队列（防止内存泄漏）
     */
    private void destroyDelayedQueue() {
        if (delayedTicketQueue != null) {
            try {
                delayedTicketQueue.destroy();
                log.info("延迟队列已销毁");
            } catch (Exception e) {
                log.warn("销毁延迟队列失败", e);
            }
        }
    }
}