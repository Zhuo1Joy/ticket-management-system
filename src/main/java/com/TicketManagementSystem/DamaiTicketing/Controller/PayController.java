package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.TicketManagementSystem.DamaiTicketing.Entity.PaymentRecord;
import com.TicketManagementSystem.DamaiTicketing.Entity.PaymentResponse;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Service.AlipayService;
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

    @Autowired
    AlipayService alipayService;


    @SaCheckLogin
    @GetMapping("/api/payment/alipay/create/{orderId}")
    @Operation(
            summary = "创建支付订单并获取支付二维码（网页）"
    )
    public Response createPayment(@PathVariable Long orderId) {
        // 创建支付记录（和二维码）
//        PaymentRecord paymentRecord = payService.createPayment(orderId);
//
//        // 获取二维码
//        String qrCodeUrl = paymentRecord.getQrCodeUrl();
//
//        // 我本来想直接返回二维码的 但是AI说前端要看 那好吧我封装一个响应类
//        PaymentResponse paymentResponse = new PaymentResponse();
//        paymentResponse.setBusinessOrderNo(paymentRecord.getBusinessOrderNo());
//        paymentResponse.setQrCodeUrl(qrCodeUrl);
//        paymentResponse.setAmount(paymentRecord.getAmount());

        String paymentUrl = payService.createPayment(orderId);

//        return Response.success(200, "成功获取支付二维码", paymentResponse);
        return Response.success(200, "成功获取支付页面", paymentUrl);

    }


    @SaCheckLogin
    @GetMapping("/api/payment/status/{businessOrderNo}")
    @Operation(
            summary = "查询订单（记录）详情"
    )
    public Response getPaymentRecordDetails(@PathVariable String businessOrderNo) {
        return Response.success(200, "返回订单状态成功", paymentRecordService.selectByBusinessOrderNo(businessOrderNo));
    }


    @SaCheckLogin
    @GetMapping("/api/payment/alipay/status/{paymentOrderNo}")
    @Operation(
            summary = "查询支付宝订单状态"
    )
    public Response getAlipayStatus(@PathVariable String paymentOrderNo) {
        return Response.success(200, "查询支付宝订单状态成功", alipayService.selectAlipayTradeStatus(paymentOrderNo));
    }


    @SaCheckLogin
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

        log.info("支付宝调取回调方法");

        // 验证回调
        boolean isValid = payService.handleAlipayCallback(params);
        if (!isValid) {
            log.error("支付宝回调签名验证失败");
            return "failure";
        }
        return "success";

    }

}