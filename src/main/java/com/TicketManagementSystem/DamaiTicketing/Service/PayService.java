package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.PaymentRecord;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketOrder;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.MQ.DelayMessageProducer;
import com.TicketManagementSystem.DamaiTicketing.MQ.PaymentSuccessMessage;
import com.TicketManagementSystem.DamaiTicketing.MQ.PaymentSuccessProducer;
import com.TicketManagementSystem.DamaiTicketing.Mapper.PaymentRecordMapper;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PayService extends ServiceImpl<PaymentRecordMapper, PaymentRecord> {

    @Autowired
    TicketOrderService ticketOrderService;

    @Autowired
    PaymentRecordService paymentRecordService;

    @Autowired
    AlipayService alipayService;

    @Autowired
    EmailService emailService;

    @Autowired
    PaymentSuccessProducer paymentSuccessProducer;

    @Autowired
    DelayMessageProducer delayMessageProducer;

    @Transactional
    public String createPayment(Long orderId) {

        TicketOrder ticketOrder = ticketOrderService.getById(orderId);

        if (LocalDateTime.now().isAfter(ticketOrder.getExpireTime())) throw new BusinessException(403, "该订单已超时，请求支付失败");

        String businessOrderNo = ticketOrder.getOrderNo();
        Long userId = ticketOrder.getUserId();
        BigDecimal amount = ticketOrder.getTotalAmount();
        String subject = ticketOrder.getOrderName();

        // 创建支付记录对象
        PaymentRecord paymentRecord = paymentRecordService.createPaymentRecord(businessOrderNo, userId, amount, subject);
        String paymentOrderNo = paymentRecord.getPaymentOrderNo();

        try {
            // 创建支付宝订单
            AlipayTradePagePayResponse response = alipayService.createAlipayTrade(paymentOrderNo, amount, subject);
            String tradeNo = response.getTradeNo();  // 支付宝交易号
            String paymentUrl = response.getBody();  // 支付宝交易网页地址

            // 更新支付记录中的支付宝订单编号
            paymentRecord.setTradeNo(tradeNo);
            updateById(paymentRecord);

            log.info("支付订单创建完成，订单号：{}，支付宝订单号：{}", businessOrderNo, tradeNo);

            delayMessageProducer.sendDelayMessage(paymentOrderNo, 30);

            return paymentUrl;

        } catch (Exception e) {
            log.error("获取支付宝支付网页失败，订单号：{}", businessOrderNo, e);
        }

        return null;
    }

    // 处理支付成功（真正方法）
    public void processPaymentSuccess(String paymentOrderNo, String tradeNo, String email) {

        // 更新支付状态（支付记录+业务订单）
        paymentRecordService.updatePaymentStatus(paymentOrderNo, 1, tradeNo);
        // 发送邮件
        emailService.sendPaymentSuccessEmail(paymentOrderNo, email);

    }

    // 处理支付成功（异步）
    public void asyncProcessPaymentSuccess(String paymentOrderNo, String tradeNo) {

        // 转换为消息类型
        PaymentSuccessMessage message = convertToMessage(paymentOrderNo, tradeNo);
        // 发送消息
        paymentSuccessProducer.sentPaymentSuccessMessage(message);

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
        String paymentOrderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

            // 3. 幂等性检查（防止重复回调）
        PaymentRecord paymentRecord = paymentRecordService.selectByPaymentOrderNo(paymentOrderNo);

        if (paymentRecord == null) {
            log.error("支付记录不存在，交易单号: {}", paymentOrderNo);
            return false;
        }

        // 获取订单号
        String businessOrderNo = paymentRecord.getBusinessOrderNo();

        if (paymentRecord.getStatus().equals(1)) {
            log.info("订单已支付，重复回调: {}", businessOrderNo);
            return true;
        }

        // 4. 验证金额（防篡改）
        BigDecimal callbackAmount = new BigDecimal(params.get("total_amount"));
        if (callbackAmount.compareTo(paymentRecord.getAmount()) != 0) {
            log.error("金额不一致，支付记录:{}，回调:{}", paymentRecord.getAmount(), callbackAmount);
            return false;
        }

        // 5. 处理不同交易状态
        // TODO 这里交易状态 前者不支持退款 后者支持 但是我暂时没办法分开它们做退款处理
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 更新支付记录
            asyncProcessPaymentSuccess(paymentOrderNo, tradeNo);
            log.info("支付成功处理完成: {}", paymentOrderNo);
            return true;
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            // 交易关闭
            paymentRecordService.closePaymentRecord(paymentOrderNo);
            if (!alipayService.closeAlipayTrade(paymentOrderNo)) {
                log.error("关闭支付宝订单失败");
                return false;
            }
            log.info("支付已关闭: {}", paymentOrderNo);
            return true;
        }
        return false;
    }

    // 关闭支付宝支付订单/取消订单
    public void cancelPayment(String paymentOrderNo) {
        // 修改支付记录中的订单状态 & 删除支付宝订单
        if (!paymentRecordService.closePaymentRecord(paymentOrderNo) || !alipayService.closeAlipayTrade(paymentOrderNo)) {
            log.error("删除订单失败");
            return;
        }
        log.info("删除支付订单成功，订单编号：{}", paymentOrderNo);
    }

    // 转换为消息类型
    public PaymentSuccessMessage convertToMessage(String paymentOrderNo, String tradeNo) {

        String requestId = UUID.randomUUID().toString();
        String businessOrderNo = paymentRecordService.selectByPaymentOrderNo(paymentOrderNo).getBusinessOrderNo();
        String email = ticketOrderService.getEmailByOrderNo(businessOrderNo);

        PaymentSuccessMessage message = new PaymentSuccessMessage();

        message.setRequestId(requestId);
        message.setBusinessOrderNo(businessOrderNo);
        message.setPaymentOrderNo(paymentOrderNo);
        message.setTradeNo(tradeNo);
        message.setEmail(email);

        return message;

    }

    // 超时关单
    // 因为聪明的支付宝会自己关单 那我只要处理自己这边的数据库就好了
    public void closeTimeOutOrder(String paymentOrderNo) {
        // 修改支付记录中的订单状态
        paymentRecordService.closePaymentRecord(paymentOrderNo);
        log.info("成功修改支付记录状态为已失败：{}", paymentOrderNo);

        // 同步更新业务订单
        String businessOrderNo = paymentRecordService.selectByPaymentOrderNo(paymentOrderNo).getBusinessOrderNo();
        ticketOrderService.cancelOrder(null, businessOrderNo, null);
        log.info("业务订单已取消支付：{}", businessOrderNo);
    }

}
