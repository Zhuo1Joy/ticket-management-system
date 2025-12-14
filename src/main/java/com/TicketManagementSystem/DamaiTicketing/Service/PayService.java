package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.PaymentRecord;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketOrder;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
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
    UserService userService;

    @Transactional
    public String createPayment(Long orderId) {

        TicketOrder ticketOrder = ticketOrderService.getById(orderId);

        String businessOrderNo = ticketOrder.getOrderNo();
        Long userId = ticketOrder.getUserId();
        BigDecimal amount = ticketOrder.getTotalAmount();
        String subject = ticketOrder.getOrderName();

        // 创建支付记录对象
        PaymentRecord paymentRecord = paymentRecordService.createPaymentRecord(businessOrderNo, userId, amount, subject);
        String paymentOrderNo = paymentRecord.getPaymentOrderNo();

        try {
            // 创建支付宝订单同时获取二维码
            AlipayTradePagePayResponse response = alipayService.createAlipayTrade(paymentOrderNo, amount, subject);
            String tradeNo = response.getTradeNo();  // 支付宝交易号
            String paymentUrl = response.getBody();

            // 更新支付记录中的支付宝订单编号
            paymentRecord.setTradeNo(tradeNo);
            updateById(paymentRecord);

            log.info("支付订单创建完成，订单号：{}，支付宝订单号：{}", businessOrderNo, tradeNo);

            return paymentUrl;

        } catch (Exception e) {
            log.error("获取支付宝支付网页失败，订单号：{}", businessOrderNo, e);
        }

        return null;

    }

    // 创建支付二维码
//    @Transactional
//    public PaymentRecord createPayment(Long orderId) {
//
//        TicketOrder ticketOrder = ticketOrderService.getById(orderId);
//
//        String businessOrderNo = ticketOrder.getOrderNo();
//        Long userId = ticketOrder.getUserId();
//        BigDecimal amount = ticketOrder.getTotalAmount();
//        String subject = ticketOrder.getOrderName();
//
//        // 创建支付记录对象
//        PaymentRecord paymentRecord = paymentRecordService.createPaymentRecord(businessOrderNo, userId, amount, subject);
//        String paymentOrderNo = paymentRecord.getPaymentOrderNo();
//
//        try {
//            // 创建支付宝订单同时获取二维码
//            AlipayTradePagePayResponse response = alipayService.createAlipayTrade(paymentOrderNo, amount, subject);
//            String tradeNo = response.getTradeNo();  // 支付宝交易号
//            String qrUrlCode = response.getQrCode();
//
//            // 更新支付记录中的二维码URL和支付宝订单编号
//            paymentRecord.setQrCodeUrl(qrCodeUrl);
//            paymentRecord.setTradeNo(tradeNo);
//            updateById(paymentRecord);
//
//            log.info("支付订单创建完成，订单号：{}，二维码：{}", orderNo, qrCodeUrl);
//
//        } catch (Exception e) {
//            log.error("获取支付宝二维码失败，订单号：{}", businessOrderNo, e);
//        }
//
//        return paymentRecord;
//
//    }

    // 处理支付成功（真正方法）
    public void processPaymentSuccess(String businessOrderNo, String tradeNo, String email) {

        // 更新支付状态
        paymentRecordService.updatePaymentStatus(businessOrderNo, 1, tradeNo);
        // 发送邮件
        emailService.sendPaymentSuccessEmail(email);

    }

    // 处理支付成功（异步）
    public void asyncProcessPaymentSuccess(String businessOrderNo, String tradeNo) {

        // 转换为消息类型
        PaymentSuccessMessage message = convertToMessage(businessOrderNo, tradeNo);
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
            asyncProcessPaymentSuccess(businessOrderNo, tradeNo);
            log.info("支付成功处理完成: {}", businessOrderNo);
            return true;
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            // 交易关闭
            paymentRecordService.closePaymentRecord(businessOrderNo);
            if (!alipayService.closeAlipayTrade(paymentOrderNo)) {
                log.error("关闭支付宝订单失败");
                return false;
            }
            log.info("支付已关闭: {}", businessOrderNo);
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

    // 转换为消息类型
    public PaymentSuccessMessage convertToMessage(String businessOrderNo, String tradeNo) {

        String requestId = UUID.randomUUID().toString();
        String email = userService.getUserInformation().getEmail();
        String paymentOrderNo = paymentRecordService.selectByBusinessOrderNo(businessOrderNo).getPaymentOrderNo();

        PaymentSuccessMessage message = new PaymentSuccessMessage();
        message.setEmail(email);
        message.setRequestId(requestId);
        message.setBusinessOrderNo(businessOrderNo);
        message.setPaymentOrderNo(paymentOrderNo);
        message.setTradeNo(tradeNo);

        return message;

    }

}
