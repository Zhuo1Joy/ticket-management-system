package com.TicketManagementSystem.DamaiTicketing.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.GrabTicketRequest;
import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceSession;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketOrder;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.TicketTierMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

// 抢票
@Slf4j
@Service
public class TicketGrabbingService extends ServiceImpl<TicketTierMapper, TicketTier> {

    @Autowired
    PerformanceSessionService performanceSessionService;
    @Autowired
    TicketTierService ticketTierService;
    @Autowired
    TicketOrderService ticketOrderService;
    @Autowired
    RedisTemplate<String, Integer> redisTemplate;

    private static final String STOCK_KEY_PREFIX = "ticket_stock:";
    private static final String SALE_SWITCH_KEY_PREFIX = "sale_switch:";

    @Transactional(rollbackFor = Exception.class)
    public String grabTicket(GrabTicketRequest grabTicketRequest) {

        Long userId = StpUtil.getLoginIdAsLong();
        Long tierId = grabTicketRequest.getTierId();
        int quantity = grabTicketRequest.getQuantity();

        // 真会出现这种情况吗 我要拷打管理员了
        TicketTier ticketTier = ticketTierService.getById(tierId);
        if (ticketTier == null) {
            throw new BusinessException(404, "票档不存在");
        }

        // 检查库存和场次状态
        if (!validateTicketTier(ticketTier, quantity)) {
            throw new BusinessException(404, "库存不足或场次未开票");
        }

        // 保存当前版本号
        Integer currentVersion = ticketTier.getVersion();

        // 检测销售开关是否打开
        String switchKey = SALE_SWITCH_KEY_PREFIX + ticketTier.getSessionId();
        Integer isOnSale = redisTemplate.opsForValue().get(switchKey);
        if (isOnSale == null || isOnSale != 1)
            throw new BusinessException(404, "暂未开票 请耐心等待");

        // Redis预扣库存
        String stockKey = STOCK_KEY_PREFIX + tierId;
        boolean redisSuccess = false;
        try {
            // Redis原子扣减
            Long remainingStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
            if (remainingStock == null || remainingStock < 0) {
                // 库存不足->回滚Redis
                redisTemplate.opsForValue().increment(stockKey, quantity);
                throw new BusinessException(401, "库存不足");
            }

            redisSuccess = true;
            log.info("✅ Redis预扣成功，剩余库存: {}", remainingStock);

            // 数据库扣减库存
            boolean dbSuccess = reduceStockInDB(tierId, quantity, currentVersion);
            if (!dbSuccess) {
                // 乐观锁失败->回滚Redis
                redisTemplate.opsForValue().increment(stockKey, quantity);
                throw new BusinessException(401, "已售空");
            }

            // 计算金额并创建预扣订单
            BigDecimal amount = calculateAmount(ticketTier, quantity);
            TicketOrder ticketOrder = ticketOrderService.createOrder(grabTicketRequest, amount);
            String orderNo = ticketOrder.getOrderNo();

            log.info("🎉 抢票成功！用户: {}, 订单: {}, 金额: {}", userId, orderNo, amount);
            return orderNo;

        } catch (BusinessException b) {
            // 直接抛出
            // 不加这个那业务异常会被下面的异常吃掉 无法返回错误响应
            throw b;
        } catch (Exception e) {
            // 丢出系统异常
            log.error("💥 抢票异常", e);
            if (redisSuccess) {
                redisTemplate.opsForValue().increment(stockKey, quantity);
            }

        }
        return null;
    }

    private boolean validateTicketTier(TicketTier ticketTier, Integer quantity) {
        // 检查库存
        int availableTickets = ticketTier.getAvailableQuantity();
        if (availableTickets < quantity) {
            log.warn("库存不足，需要 {}，可用 {}", quantity, availableTickets);
            return false;
        }

        // 检查场次是否开票
        PerformanceSession session = performanceSessionService.getById(ticketTier.getSessionId());
        if (session == null || !session.getIsOnSale()) {
            log.warn("场次未开票: {}", ticketTier.getSessionId());
            return false;
        }

        return true;

    }

    // 数据库扣减库存（乐观锁）
    private boolean reduceStockInDB(Long tierId, Integer quantity, Integer version) {

        boolean result = this.lambdaUpdate()
                .eq(TicketTier::getId, tierId)
                .eq(TicketTier::getVersion, version)
                .ge(TicketTier::getAvailableQuantity, quantity)
                .setSql("available_quantity = available_quantity - " + quantity)
                .setSql("version = version + 1")
                .update();

        if (result) {
            log.info("✅ 数据库扣减成功，票档: {}，数量: {}", tierId, quantity);
        } else {
            log.warn("❌ 数据库扣减失败，票档: {}，被其他人抢先了", tierId);
        }
        return result;

    }

    // 计算订单金额
    private BigDecimal calculateAmount(TicketTier ticketTier, Integer quantity) {

        BigDecimal amount = ticketTier.getPrice(); // 差点忘了当时设置的Price就是BigDecimal类型的hh
        return amount.multiply(new BigDecimal(quantity));

    }

}
