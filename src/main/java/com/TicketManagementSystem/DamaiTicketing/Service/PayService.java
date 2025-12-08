package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.PaymentRecord;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketOrder;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.PaymentRecordMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class PayService extends ServiceImpl<PaymentRecordMapper, PaymentRecord> {

    @Autowired
    TicketOrderService ticketOrderService;

    @Autowired
    PaymentRecordService paymentRecordService;

    @Autowired
    AlipayService alipayService;

    // 创建支付二维码
    @Transactional
    public PaymentRecord createPayment(Long orderId) {

        TicketOrder ticketOrder = ticketOrderService.getById(orderId);

        String orderNo = ticketOrder.getOrderNo();
        Long userId = ticketOrder.getUserId();
        BigDecimal amount = ticketOrder.getTotalAmount();
        String subject = ticketOrder.getOrderName();

        // 创建支付记录对象
        PaymentRecord paymentRecord = paymentRecordService.createPaymentRecord(orderNo, userId, amount, subject);

        try {
            // 创建支付宝订单同时获取二维码
            String qrCodeUrl = alipayService.createAlipayTrade(orderNo, amount, subject);

            // 更新支付记录中的二维码URL
            paymentRecord.setQrCodeUrl(qrCodeUrl);
            updateById(paymentRecord);

            log.info("支付订单创建完成，订单号：{}，二维码：{}", orderNo, qrCodeUrl);

        } catch (Exception e) {
            log.error("获取支付宝二维码失败，订单号：{}", orderNo, e);
        }

        return paymentRecord;

    }

    // 处理支付成功
    public void processPaymentSuccess(String orderNo, String tradeNo) {
        // TODO 发送邮件通知
        paymentRecordService.updatePaymentStatus(orderNo, 1, tradeNo);
        log.info("支付状态已更新:1-支付成功，订单号：{}", orderNo);
    }

    // 支付回调
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAlipayCallback(Map<String, String> params) {
        // 1. 验证签名（防伪造）
        boolean isValid = alipayService.verifySignature(params);
        if (!isValid) {
            log.warn("支付宝回调签名验证失败: {}", params);
            return false;
        }

        // 2. 获取支付流水号
        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        // 3. 幂等性检查（防止重复回调）
        PaymentRecord paymentRecord = paymentRecordService.getById(orderNo);
        if (paymentRecord == null) {
            log.error("支付记录不存在: {}", orderNo);
            return false;
        }

        if (paymentRecord.getStatus().equals(1)) {
            log.info("订单已支付，重复回调: {}", orderNo);
            return true;
        }

        // 4. 验证金额（防篡改）
        BigDecimal callbackAmount = new BigDecimal(params.get("total_amount"));
        if (callbackAmount.compareTo(paymentRecord.getAmount()) != 0) {
            log.error("金额不一致，支付记录:{}，回调:{}", paymentRecord.getAmount(), callbackAmount);
            return false;
        }

        // 5. 处理不同交易状态
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 更新支付记录
            paymentRecordService.updatePaymentStatus(orderNo, 1, tradeNo);

            // TODO 发送支付成功MQ消息 异步更新订单 发送邮件

            log.info("支付成功处理完成: {}", orderNo);
            return true;

        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            // 交易关闭
            paymentRecordService.closePaymentRecord(orderNo);
            if (!alipayService.closeAlipayTrade(tradeNo)) {
                log.error("关闭支付宝订单失败");
                return false;
            }
            log.info("支付已关闭: {}", orderNo);
            return true;
        }
        return false;
    }

    // 关闭支付宝支付订单
    public void cancelPayment(String orderNo) {
        // 修改支付记录中的订单状态 & 删除支付宝订单
        if (!paymentRecordService.closePaymentRecord(orderNo) || !alipayService.closeAlipayTrade(orderNo)) {
            log.error("删除订单失败");
            throw new BusinessException(401, "删除订单失败");
        }
        log.info("删除订单成功，订单编号：{}", orderNo);
    }

}
