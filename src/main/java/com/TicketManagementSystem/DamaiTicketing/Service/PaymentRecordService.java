package com.TicketManagementSystem.DamaiTicketing.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.PaymentRecord;
import com.TicketManagementSystem.DamaiTicketing.Enums.RecordStatus;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.PaymentRecordMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class PaymentRecordService extends ServiceImpl<PaymentRecordMapper, PaymentRecord> {

    final
    TicketOrderService ticketOrderService;

    @Value("${payment.timeout-minutes:30}0")
    public Integer timeoutMinutes;

    public PaymentRecordService(TicketOrderService ticketOrderService) {
        this.ticketOrderService = ticketOrderService;
    }

    // 创建支付记录
    // 如果存在支付记录我可以理解为已经到支付宝创建过支付订单了吧
    // TODO 需要改进 事务一致性
    @Transactional
    public PaymentRecord createPaymentRecord(String businessOrderNo, Long userId, BigDecimal amount, String subject) {

        log.info("开始创建支付订单，订单号：{}，用户ID：{}，金额：{}", businessOrderNo, userId, amount);

        // 检查是否已存在支付记录->保证幂等性
        PaymentRecord result = this.lambdaQuery()
                .eq(PaymentRecord::getBusinessOrderNo, businessOrderNo)
                .one();
        if (result != null) {
            log.info("支付记录已存在，直接返回，订单号：{}", businessOrderNo);
            throw new BusinessException(400, "无法创建支付订单->订单已存在");
        }

        long timestamp = System.currentTimeMillis();
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);

        // 创建支付记录对象
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setBusinessOrderNo(businessOrderNo);
        paymentRecord.setPaymentOrderNo("PAY_" + timestamp + randomNum);
        paymentRecord.setAmount(amount);
        paymentRecord.setUserId(userId);
        paymentRecord.setStatus(RecordStatus.PENDING);
        paymentRecord.setSubject(subject);
        paymentRecord.setExpireTime(LocalDateTime.now().plusMinutes(timeoutMinutes));

        // 保存到数据库
        save(paymentRecord);
        log.info("支付记录保存成功，ID：{}", paymentRecord.getId());

        return paymentRecord;

    }

    // 根据订单号查询支付记录
    public PaymentRecord selectByBusinessOrderNo(String businessOrderNo) {
        PaymentRecord result = this.lambdaQuery()
                .eq(PaymentRecord::getUserId, StpUtil.getLoginIdAsLong())
                .eq(PaymentRecord::getBusinessOrderNo, businessOrderNo)
                .one();

        if (result == null) throw new BusinessException(404, "暂无相关订单");
        return result;
    }

    // 根据交易号查询支付记录
    public PaymentRecord selectByPaymentOrderNo(String paymentOrderNo) {
        return this.lambdaQuery()
                .eq(PaymentRecord::getUserId, StpUtil.getLoginIdAsLong())
                .eq(PaymentRecord::getPaymentOrderNo, paymentOrderNo)
                .one();
    }

    // 更新支付状态为成功
    @Transactional
    public void updatePaymentStatus(PaymentRecord paymentRecord, String tradeNo) {

        paymentRecord.setStatus(RecordStatus.PAID);
        if (tradeNo != null) {
            paymentRecord.setTradeNo(tradeNo);
        }else log.error("支付宝交易编号:tradeNo 为空");

        // 支付时间
        paymentRecord.setPayTime(LocalDateTime.now());

        boolean result = saveOrUpdate(paymentRecord);
        log.info("更新支付记录状态，支付订单号：{}，状态：{}，结果：{}",
                paymentRecord.getPaymentOrderNo(), paymentRecord.getStatus(), result ? "成功" : "失败");

    }

    // 关闭支付订单（手动关闭版）
    public boolean closePaymentRecordByHand(String paymentOrderNo) {

        boolean result = this.lambdaUpdate()
                .eq(PaymentRecord::getUserId, StpUtil.getLoginIdAsLong())
                .eq(PaymentRecord::getPaymentOrderNo, paymentOrderNo)
                .set(PaymentRecord::getStatus, RecordStatus.CLOSED)
                .update();
        if (!result) {
            log.error("关闭订单失败");
            return false;
        }
        return true;

    }

    // 关闭支付订单（自动调用版）
    public void closePaymentRecord(String paymentOrderNo) {

        boolean result = this.lambdaUpdate()
                .eq(PaymentRecord::getPaymentOrderNo, paymentOrderNo)
                .set(PaymentRecord::getStatus, RecordStatus.CLOSED)
                .update();
        if (!result) {
            log.error("自动关闭订单失败");
        }

    }

    // 查询需要补偿的订单
    public List<PaymentRecord> selectOrdersNeedCompensation() {
        // 查询条件:状态为待支付（0）
        return this.lambdaQuery()
                .eq(PaymentRecord::getStatus, RecordStatus.CLOSED)
                .gt(PaymentRecord::getExpireTime, LocalDateTime.now())
                .list();
    }

}
