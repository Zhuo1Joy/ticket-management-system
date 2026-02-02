package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.Performance;
import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceSession;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTask;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RDelayedQueue;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AutoStartTicketService {

    final
    PerformanceService performanceService;

    final
    PerformanceSessionService performanceSessionService;

    final
    TicketTierService ticketTierService;

    final
    RedisTemplate<String, Integer> redisTemplate;

    final RDelayedQueue<TicketTask> delayedTicketQueue;

    // Redis 键常量
    private static final String STOCK_KEY_PREFIX = "ticket_stock:";
    private static final String SALE_SWITCH_KEY_PREFIX = "sale_switch:";
    private static final String OPENING_LOCK_KEY = "ticket_opening_lock";

    public AutoStartTicketService(PerformanceService performanceService,
                                  PerformanceSessionService performanceSessionService,
                                  TicketTierService ticketTierService,
                                  RedisTemplate<String, Integer> redisTemplate,
                                  RDelayedQueue<TicketTask> delayedTicketQueue) {
        this.performanceService = performanceService;
        this.performanceSessionService = performanceSessionService;
        this.ticketTierService = ticketTierService;
        this.redisTemplate = redisTemplate;
        this.delayedTicketQueue = delayedTicketQueue;
    }

    // 每10分钟查找一次即将要开票的演出
    @Scheduled(cron = "0 0/10 * * * ?")
    public void regularlySearchPerformances() {
        // 寻找十分钟内要开票的演出
        List<Performance> performances = getWaitOpenPerformances();
        for (Performance performance : performances) {
            addDelayTask(performance);
        }

    }

    // 初始化 Redis库存
    public void preloadStockBeforeOpening(Long performanceId) {
        log.info("🚀 开始预加载Redis库存");

        try {
            if (performanceId == null) {
                log.info("当前没有需要预加载库存的演出");
                return;
            }

            // 获取所有相关场次
            List<PerformanceSession> sessions = performanceSessionService.lambdaQuery()
                    .eq(PerformanceSession::getPerformanceId, performanceId)
                    .list();

            if (sessions.isEmpty()) {
                log.info("没有找到相关场次");
                return;
            }

            // 获取场次 ID
            List<Long> sessionIds = sessions.stream()
                    .map(PerformanceSession::getId)
                    .toList();

            log.info("预加载 {} 个场次的库存", sessionIds.size());

            // 查询这些场次的所有票档
            List<TicketTier> ticketTiers = ticketTierService.lambdaQuery()
                    .in(TicketTier::getSessionId, sessionIds)
                    .list();

            if (ticketTiers.isEmpty()) {
                log.warn("没有找到票档数据");
                return;
            }

            int successCount = 0;
            for (TicketTier ticketTier : ticketTiers) {
                try {
                    String stockKey = STOCK_KEY_PREFIX + ticketTier.getId();
                    String switchKey = SALE_SWITCH_KEY_PREFIX + ticketTier.getSessionId();

                    // 设置库存到Redis，24小时过期
                    redisTemplate.opsForValue().set(stockKey, ticketTier.getAvailableQuantity(), Duration.ofHours(24));

                    // 设置销售开关为关闭
                    redisTemplate.opsForValue().set(switchKey, 0, Duration.ofHours(24));

                    successCount++;

                } catch (Exception e) {
                    log.error("初始化票档 {} 失败", ticketTier.getId(), e);
                }
            }

            log.info("✅ 库存预加载完成：成功 {} / 总数 {}，销售开关：关闭",
                    successCount, ticketTiers.size());

        } catch (Exception e) {
            log.error("❌ 库存预加载失败", e);
        }
    }

    // 执行开票操作
    @Transactional
    public void openTicket(Long performanceId) {

        String lockKey = OPENING_LOCK_KEY;

        // 获取全局锁
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, 1, 30, TimeUnit.SECONDS);

        if (performanceId == null) {
            log.info("没有需要开票的演出");
            return;
        }

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 更新数据库 将相关场次标记为已开票
                performanceSessionService.lambdaUpdate()
                        .eq(PerformanceSession::getPerformanceId, performanceId)
                        .eq(PerformanceSession::getIsOnSale, 0)      // 只更新未开票的
                        .set(PerformanceSession::getIsOnSale, 1)     // 设置为已开票
                        .update();

                // 打开 Redis销售开关
                openSaleSwitch(performanceId);

                log.info("✅ 开票成功：演出ID={}", performanceId);
            } catch (Exception e) {
                log.error("❌ 开票失败：演出ID={}", performanceId, e);
                throw new RuntimeException("开票操作失败", e);
            } finally {
                // 释放锁
                redisTemplate.delete(lockKey);
            }
        } else log.debug("⏳ 其他服务正在处理开票：演出ID={}", performanceId);

    }

    // 打开 Redis销售开关
    private void openSaleSwitch(Long performanceId) {
        // 获取所有相关场次
        List<PerformanceSession> sessions = performanceSessionService.lambdaQuery()
                .eq(PerformanceSession::getPerformanceId, performanceId)
                .list();

        for (PerformanceSession session : sessions) {
            String switchKey = SALE_SWITCH_KEY_PREFIX + session.getId();
            redisTemplate.opsForValue().set(switchKey, 1, Duration.ofHours(24));
        }

        log.info("已打开 {} 个场次的销售开关", sessions.size());
    }

    // 查找等待开票的演出
    private List<Performance> getWaitOpenPerformances() {

        LocalDateTime now = LocalDateTime.now().plus(Duration.ofMinutes(10));
        return performanceService.lambdaQuery()
                .between(Performance::getTicketStartTime, LocalDateTime.now(), now)
                .eq(Performance::getStatus, 1)
                .list();

    }

    // 添加延迟任务
    public void addDelayTask(Performance performance) {
        TicketTask task = new TicketTask();
        task.setTaskType("INIT_STOCK");
        task.setTaskId(performance.getId());

        long timeToOpen = Duration.between(LocalDateTime.now(), performance.getTicketStartTime()).toSeconds();
        log.info("距离开票还有：{}秒", timeToOpen);
        // 发送延迟消息->库存预加载
        if (timeToOpen > 300) {
            delayedTicketQueue.offer(task, timeToOpen - 300, TimeUnit.SECONDS);
        } else {
            // 时间不足五分钟则直接开始库存初始化
            preloadStockBeforeOpening(performance.getId());
        }

        // 发送开票延迟消息
        TicketTask ticketTask = new TicketTask();
        ticketTask.setTaskType("OPEN_TICKET");
        ticketTask.setTaskId(performance.getId());
        delayedTicketQueue.offer(ticketTask, timeToOpen, TimeUnit.SECONDS);
    }

}