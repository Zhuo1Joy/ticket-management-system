package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Service.TicketOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@Tag(name = "订单模块", description = "订单相关的所有操作接口")
public class TicketOrderController {

    final
    TicketOrderService ticketOrderService;

    public TicketOrderController(TicketOrderService ticketOrderService) {
        this.ticketOrderService = ticketOrderService;
    }

    @SaCheckLogin
    @GetMapping("/api/order")
    @Operation(
        summary = "分页查询订单列表"
    )
    public Response getOrderList(@RequestParam(required = false) String performanceTitle,
                                 @RequestParam(required = false, defaultValue = "1") int pageNum) {
        return Response.success(200, "查询成功", ticketOrderService.getOrderList(performanceTitle, pageNum));
    }

    @SaCheckLogin
    @GetMapping("/api/order/{orderId}/details")
    @Operation(
            summary = "获取订单详情"
    )
    public Response getOrderDetails(@PathVariable Long orderId) {
        return Response.success(200, "查询成功", ticketOrderService.getOrderDetails(orderId));
    }

    @SaCheckLogin
    @PutMapping("/api/orde/cancel")
    @Operation(
            summary = "取消订单"
    )
    public Response deleteOrder(@RequestParam(required = false) Long id,
                                @RequestParam(required = false) String orderNo,
                                @RequestParam(required = false) String cancelReason) {
        ticketOrderService.cancelOrder(id, orderNo, cancelReason);
        return Response.success(200, "取消订单成功");
    }

    @SaCheckLogin
    @DeleteMapping("/api/order/{orderId}/delete")
    @Operation(
            summary = "删除订单"
    )
    public Response deleteOrder(@PathVariable Long orderId) {
        ticketOrderService.deleteOrder(orderId);
        return Response.success(200, "删除成功");
    }

}
