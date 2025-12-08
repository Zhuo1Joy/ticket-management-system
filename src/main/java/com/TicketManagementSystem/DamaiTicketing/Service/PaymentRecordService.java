package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.PaymentRecord;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.PaymentRecordMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentRecordService extends ServiceImpl<PaymentRecordMapper, PaymentRecord> {

    @Value("${payment.timeout-minutes:30}")
    private Integer timeoutMinutes;

    // 创建支付记录
    // TODO 需要改进 事务一致性
    @Transactional
    public PaymentRecord createPaymentRecord(String orderNo, Long userId, BigDecimal amount, String subject) {

        log.info("开始创建支付订单，订单号：{}，用户ID：{}，金额：{}", orderNo, userId, amount);

        // 检查是否已存在支付记录->保证幂等性
        PaymentRecord result = this.lambdaQuery()
                .eq(PaymentRecord::getOrderNo, orderNo)
                .one();
        if (result != null) {
            log.info("支付记录已存在，直接返回，订单号：{}", orderNo);
            return result;
        }

        // 创建支付记录对象
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderNo(orderNo);
        paymentRecord.setAmount(amount);
        paymentRecord.setUserId(userId);
        paymentRecord.setStatus(0);
        paymentRecord.setSubject(subject);
        paymentRecord.setExpireTime(LocalDateTime.now().plusMinutes(timeoutMinutes));

        // 保存到数据库
        save(paymentRecord);
        log.info("支付记录保存成功，ID：{}", paymentRecord.getId());

        return paymentRecord;

    }

    // 根据订单号查询支付记录
    public PaymentRecord selectByOrderNo(String orderNo) {
        return this.lambdaQuery()
                .eq(PaymentRecord::getOrderNo, orderNo)
                .one();
    }

    // 更新支付状态
    // TODO 需要一个枚举类来明确状态
    @Transactional
    public void updatePaymentStatus(String orderNo, Integer status, String tradeNo) {

        PaymentRecord paymentRecord = selectByOrderNo(orderNo);

        if (paymentRecord == null) {
            log.error("支付记录不存在，订单号：{}", orderNo);
            throw new BusinessException(404, "您查询的支付记录不存在");
        }

        paymentRecord.setStatus(status);
        if (tradeNo != null) {
            paymentRecord.setTradeNo(tradeNo);
        }

        if (status.equals(1)) {
            paymentRecord.setPayTime(LocalDateTime.now());
        }

        boolean result = save(paymentRecord);
        log.info("更新支付状态，订单号：{}，状态：{}，结果：{}",
                orderNo, status, result ? "成功" : "失败");

    }

    // 关闭支付订单
    public boolean closePaymentRecord(String orderNo) {

        boolean result = this.lambdaUpdate()
                .eq(PaymentRecord::getOrderNo, orderNo)
                .set(PaymentRecord::getStatus, 3)
                .update();
        if (!result) {
            log.error("关闭订单失败");
            return false;
        }
        return true;

    }

}
