package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.PaymentRecord;
import com.TicketManagementSystem.DamaiTicketing.Enums.RecordStatus;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PayCompensateService {

    final
    RedisTemplate<String, String> redisTemplate;

    final
    PaymentRecordService paymentRecordService;

    final
    AlipayService alipayService;

    final
    PayService payService;

    public PayCompensateService(RedisTemplate<String, String> redisTemplate, PaymentRecordService paymentRecordService, AlipayService alipayService, PayService payService) {
        this.redisTemplate = redisTemplate;
        this.paymentRecordService = paymentRecordService;
        this.alipayService = alipayService;
        this.payService = payService;
    }

    // 每五分钟检测一次
    @Scheduled(cron = "0 */15 * * * *")
    public void regularDetectCompensation() {
        String lockKey = "payment:compensation:polling:lock";

        try {
            // 获取分布式锁 防止多实例重复执行
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);

            if (!Boolean.TRUE.equals(locked)) {
                log.debug("补偿轮询任务正在执行（跳过本次）");
                return;
            }

            log.info("开始执行支付补偿轮询任务");

            // 执行轮询补偿
            handlePaymentCompensation();

            log.info("支付补偿轮询任务执行完成");

        } catch (Exception e) {
            log.error("支付补偿轮询任务执行异常", e);
        } finally {
            // 释放锁（注意：如果任务执行超过30秒 锁会自动过期）
            redisTemplate.delete(lockKey);
        }
    }

    // 执行轮询补偿逻辑
    public void handlePaymentCompensation() {
        // 1. 查询需要补偿的订单
        List<PaymentRecord> orders = paymentRecordService.selectOrdersNeedCompensation();

        if (CollectionUtils.isEmpty(orders)) {
            log.info("没有需要补偿的订单");
            return;
        }

        log.info("发现 {} 个需要补偿的订单", orders.size());

        // 2. 分批处理
        for (PaymentRecord order : orders) {
            try {
                boolean result = processOneOrder(order);
                if (result) log.info("处理补偿订单成功: {}", order.getPaymentOrderNo());

                // 控制请求频率 避免触发支付宝限流
                Thread.sleep(200);

            } catch (Exception e) {
                log.error("处理补偿订单失败: {}", order.getPaymentOrderNo(), e);
            }
        }
    }

    // 处理单个订单的补偿
    private boolean processOneOrder(PaymentRecord order) {
        String paymentOrderNo = order.getPaymentOrderNo();

        // 检查订单是否需要补偿
        if (!order.getStatus().equals(RecordStatus.PENDING)) {
            log.debug("订单 {} 不需要补偿，跳过", paymentOrderNo);
            return false;
        }

        // 获取订单级别的锁，防止并发处理
        String orderLockKey = "payment:compensation:order:lock:" + paymentOrderNo;
        Boolean orderLocked = redisTemplate.opsForValue()
                .setIfAbsent(orderLockKey, "1", 5, TimeUnit.MINUTES);

        if (!Boolean.TRUE.equals(orderLocked)) {
            log.debug("订单 {} 正在被处理，跳过", paymentOrderNo);
            return false;
        }

        try {
            // 1. 查询支付宝订单状态
            AlipayTradeQueryResponse response = alipayService.queryAlipayOrderStatus(paymentOrderNo);

            if (response == null) {
                log.warn("支付宝查询订单不存在: {}", paymentOrderNo);
                return false;
            }

            // 2. 处理查询结果
            return handleAlipayResponse(order, response);

        } catch (Exception e) {
            log.error("处理订单补偿异常: {}", paymentOrderNo, e);
            return false;
        } finally {
            // 释放订单锁
            redisTemplate.delete(orderLockKey);
        }
    }

    // 处理支付宝查询结果
    public boolean handleAlipayResponse(PaymentRecord order, AlipayTradeQueryResponse response) {
        String paymentOrderNo = order.getPaymentOrderNo();

        // 检查接口调用是否成功
        if (!response.isSuccess()) {
            log.warn("支付宝查询失败，支付订单: {}，错误码: {}，错误信息: {}",
                    paymentOrderNo, response.getSubCode(), response.getSubMsg());
        }

        String tradeStatus = response.getTradeStatus();
        String tradeNo = response.getTradeNo();

        log.info("支付宝查询结果，支付订单: {}，状态: {}，支付宝交易号: {}",
                paymentOrderNo, tradeStatus, tradeNo);

        // 根据交易状态处理
        switch (tradeStatus) {
            case "TRADE_SUCCESS", "TRADE_FINISHED" -> {
                // 支付宝已支付，但本地未收到回调 -> 掉单
                return handleTradeSuccess(response);
            }
            case "WAIT_BUYER_PAY" -> {
                // 等待支付，不做处理
                log.debug("订单 {} 等待用户支付", paymentOrderNo);
                return false;
            }
            case "TRADE_CLOSED" -> {
                // 交易关闭
                return handleTradeClosed(order);
            }
            default -> {
                log.warn("未知交易状态: {}，支付订单: {}", tradeStatus, paymentOrderNo);
                return false;
            }
        }
    }

    // 处理支付成功的掉单
    public boolean handleTradeSuccess(AlipayTradeQueryResponse response) {
        String paymentOrderNo = response.getOutTradeNo();
        String tradeNo = response.getTradeNo();

        log.warn("检测到掉单！订单: {}，支付宝交易号: {}", paymentOrderNo, tradeNo);

        try {
            // 3. 调用成功支付方法
            payService.asyncProcessPaymentSuccess(paymentOrderNo, tradeNo);

            log.info("掉单补偿成功，订单: {}，支付宝交易号: {}", paymentOrderNo, tradeNo);
            return true;

        } catch (Exception e) {
            log.error("处理支付成功掉单失败，订单: {}", paymentOrderNo, e);
            return false;
        }
    }

    // 处理交易关闭的掉单
    public boolean handleTradeClosed(PaymentRecord order) {
        String paymentOrderNo = order.getPaymentOrderNo();

        log.info("订单 {} 已关闭，更新本地状态", paymentOrderNo);

        try {
            // 更新订单状态为关闭
            paymentRecordService.closePaymentRecord(paymentOrderNo);
            return true;
        } catch (Exception e) {
            log.error("更新订单关闭状态失败，支付订单号: {}", paymentOrderNo, e);
            return false;
        }
    }

}
