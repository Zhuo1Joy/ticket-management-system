package com.TicketManagementSystem.DamaiTicketing.Controller;

import com.TicketManagementSystem.DamaiTicketing.Entity.PaymentRecord;
import com.TicketManagementSystem.DamaiTicketing.Entity.PaymentResponse;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Service.PaymentRecordService;
import com.TicketManagementSystem.DamaiTicketing.Service.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@Tag(name = "支付模块", description = "支付相关的操作接口（支付宝沙箱环境）")
public class PayController {

    @Autowired
    PayService payService;

    @Autowired
    PaymentRecordService paymentRecordService;

    @GetMapping("/api/payment/alipay/create/{orderId}")
    @Operation(
            summary = "创建支付订单并获取支付二维码"
    )
    public Response createPayment(@PathVariable Long orderId) {
        // 创建支付记录（和二维码）
        PaymentRecord paymentRecord = payService.createPayment(orderId);

        // 获取二维码
        String qrCodeUrl = paymentRecord.getQrCodeUrl();

        // 我本来想直接返回二维码的 但是AI说前端要看 那好吧我封装一个响应类
        PaymentResponse response = new PaymentResponse();
        response.setOrderNo(paymentRecord.getOrderNo());
        response.setQrCodeUrl(qrCodeUrl);
        response.setAmount(paymentRecord.getAmount());

        return Response.success(200, "成功获取支付二维码", response);

    }

    @GetMapping("/api/payment/status/{orderNo}")
    @Operation(
            summary = "查询订单（记录）详情"
    )
    public Response getPaymentStatus(@PathVariable String orderNo) {
        return Response.success(200, "返回订单状态成功", paymentRecordService.selectByOrderNo(orderNo));
    }

    @DeleteMapping("/api/payment/cancel/{orderNo}")
    @Operation(
            summary = "取消支付 即取消支付宝支付订单"
    )
    public Response cancelPayment(@PathVariable String orderNo) {
        payService.cancelPayment(orderNo);
        return Response.success(200, "取消支付成功");
    }

    @PostMapping("/api/payment/alipay/notify")
    @Operation(
            summary = "支付宝回调通知"
    )
    public String handleAlipayNotify(@RequestParam Map<String, String> params) {

        // 验证回调
        boolean isValid = payService.handleAlipayCallback(params);

        if (!isValid) {
            log.error("支付宝回调签名验证失败");
            return "failure";
        }

        String tradeStatus = params.get("trade_status");
        String orderNo = params.get("out_trade_no");

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            payService.processPaymentSuccess(orderNo, params.get("trade_no"));
            return "success";
        }
        return "failure";

    }

}