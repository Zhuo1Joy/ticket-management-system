package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.Performance;
import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceSession;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AutoStartTicketService {

    @Autowired
    PerformanceService performanceService;

    @Autowired
    PerformanceSessionService performanceSessionService;

    @Autowired
    TicketTierService ticketTierService;

    @Autowired
    RedisTemplate<String, Integer> redisTemplate;

    // Redis键常量
    private static final String STOCK_KEY_PREFIX = "ticket_stock:";
    private static final String SALE_SWITCH_KEY_PREFIX = "sale_switch:";
    private static final String OPENING_LOCK_KEY = "ticket_opening_lock";

    // 初始化Redis库存
    @Scheduled(cron = "0 8 19 * * ?")
    public void preloadStockBeforeOpening() {
        log.info("🚀 开始预加载Redis库存");

        try {
            List<Performance> performances = getTodayPerformances();
            if (performances.isEmpty()) {
                log.info("今天没有需要预加载库存的演出");
                return;
            }

            List<Long> performanceIds = performances.stream()
                    .map(Performance::getId)
                    .toList();

            // 获取所有相关场次
            List<PerformanceSession> sessions = performanceSessionService.lambdaQuery()
                    .in(PerformanceSession::getPerformanceId, performanceIds)
                    .list();

            if (sessions.isEmpty()) {
                log.info("没有找到相关场次");
                return;
            }

            // 获取场次ID
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
    @Scheduled(cron = "0 10 19 * * ?")
    public void openTicket() {

        String lockKey = OPENING_LOCK_KEY;

        // 获取全局锁
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, 1, 30, TimeUnit.SECONDS);

        // 查找今天开票的演出
        List<Performance> performances = this.getTodayPerformances();

        if (performances.isEmpty()) {
            log.info("没有需要开票的演出");
            return;
        }

        List<Long> performanceIds = performances.stream()
                .map(Performance::getId)
                .toList();

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 更新数据库 将相关场次标记为已开票
                performanceSessionService.lambdaUpdate()
                        .in(PerformanceSession::getPerformanceId, performanceIds)
                        .eq(PerformanceSession::getIsOnSale, 0)      // 只更新未开票的
                        .set(PerformanceSession::getIsOnSale, 1)     // 设置为已开票
                        .update();

                // 打开Redis销售开关
                openSaleSwitch(performanceIds);

                log.info("✅ 开票成功：演出ID={}", performanceIds);
            } catch (Exception e) {
                log.error("❌ 开票失败：演出ID={}", performanceIds, e);
                throw new RuntimeException("开票操作失败", e);
            } finally {
                // 释放锁
                redisTemplate.delete(lockKey);
            }
        } else  log.debug("⏳ 其他服务正在处理开票：演出ID={}", performanceIds);

    }

    // 打开Redis销售开关
    private void openSaleSwitch(List<Long> performanceIds) {
        // 获取所有相关场次
        List<PerformanceSession> sessions = performanceSessionService.lambdaQuery()
                .in(PerformanceSession::getPerformanceId, performanceIds)
                .list();

        for (PerformanceSession session : sessions) {
            String switchKey = SALE_SWITCH_KEY_PREFIX + session.getId();
            redisTemplate.opsForValue().set(switchKey, 1, Duration.ofHours(24));
        }

        log.info("已打开 {} 个场次的销售开关", sessions.size());
    }

    // 查找今天开票的演出
    private List<Performance> getTodayPerformances() {

        LocalDate today = LocalDate.now();
        return performanceService.lambdaQuery()
                .like(Performance::getTicketStartTime, today)
                .eq(Performance::getStatus, 1)
                .list();

    }

}