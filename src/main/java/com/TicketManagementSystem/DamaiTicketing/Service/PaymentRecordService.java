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
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class PaymentRecordService extends ServiceImpl<PaymentRecordMapper, PaymentRecord> {

    @Value("${payment.timeout-minutes:30}")
    private Integer timeoutMinutes;

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
        paymentRecord.setPaymentOrderNo("PAY_"+timestamp+randomNum);
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
    public PaymentRecord selectByBusinessOrderNo(String businessOrderNo) {
        return this.lambdaQuery()
                .eq(PaymentRecord::getBusinessOrderNo, businessOrderNo)
                .one();
    }

    // 根据交易号查询支付记录
    public PaymentRecord selectByPaymentOrderNo(String paymentOrderNo) {
        return this.lambdaQuery()
                .eq(PaymentRecord::getBusinessOrderNo, paymentOrderNo)
                .one();
    }

    // 更新支付状态
    // TODO 需要一个枚举类来明确状态
    @Transactional
    public void updatePaymentStatus(String businessOrderNo, Integer status, String tradeNo) {

        PaymentRecord paymentRecord = selectByBusinessOrderNo(businessOrderNo);

        paymentRecord.setStatus(status);
        if (tradeNo != null) {
            paymentRecord.setTradeNo(tradeNo);
        }

        // 支付时间
        if (status.equals(1)) {
            paymentRecord.setPayTime(LocalDateTime.now());
        }

        boolean result = save(paymentRecord);
        log.info("更新支付状态，订单号：{}，状态：{}，结果：{}",
                businessOrderNo, status, result ? "成功" : "失败");

    }

    // 关闭支付订单
    public boolean closePaymentRecord(String businessOrderNo) {

        boolean result = this.lambdaUpdate()
                .eq(PaymentRecord::getBusinessOrderNo, businessOrderNo)
                .set(PaymentRecord::getStatus, 3)
                .update();
        if (!result) {
            log.error("关闭订单失败");
            return false;
        }
        return true;

    }

}
