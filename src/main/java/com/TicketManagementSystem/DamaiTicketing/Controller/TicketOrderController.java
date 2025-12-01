package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Service.TicketOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "订单模块", description = "订单相关的所有操作接口")
public class TicketOrderController {

    @Autowired
    TicketOrderService ticketOrderService;

    @SaCheckLogin
    @GetMapping("/api/order")
    @Operation(
        summary = "分页查询订单列表"
    )
    public Response getOrderList(@RequestParam(required = false) String performanceTitle) {
        return Response.success(200, "查询成功", ticketOrderService.getOrderList(performanceTitle));
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
    @DeleteMapping("/api/order/{orderId}/delete")
    @Operation(
            summary = "删除订单"
    )
    public Response deleteOrder(@PathVariable Long orderId) {
        ticketOrderService.deleteOrder(orderId);
        return Response.success(200, "删除成功");
    }

}
