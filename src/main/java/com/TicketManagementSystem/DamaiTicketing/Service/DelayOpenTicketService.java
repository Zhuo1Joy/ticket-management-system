package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTask;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelayOpenTicketService {

    final
    AutoStartTicketService autoStartTicketService;

    final
    RBlockingDeque<TicketTask> ticketBlockingDeque;

    public DelayOpenTicketService(AutoStartTicketService autoStartTicketService, RBlockingDeque<TicketTask> ticketBlockingDeque) {
        this.autoStartTicketService = autoStartTicketService;
        this.ticketBlockingDeque = ticketBlockingDeque;
    }

    @PostConstruct
    public void startTicketTask() {
        new Thread(() -> {
            while (true) {
                try {
                    TicketTask task = ticketBlockingDeque.take();

                    switch (task.getTaskType()) {
                        case "INIT_STOCK":
                            autoStartTicketService.preloadStockBeforeOpening(task.getTaskId());
                            break;
                        case "OPEN_TICKET":
                            autoStartTicketService.openTicket(task.getTaskId());
                            break;
                        default:
                            System.err.println("未知任务类型: " + task.getTaskType());
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

}
