package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketOrder;
import com.TicketManagementSystem.DamaiTicketing.Service.AdminOrderService;
import com.TicketManagementSystem.DamaiTicketing.Util.StpAdminUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "订单管理", description = "订单管理相关的所有操作接口")
@SaCheckPermission(value = "admin", type = StpAdminUtil.TYPE)
public class AdminOrderController {

    @Autowired
    AdminOrderService adminOrderService;

    @GetMapping("/api/admin/order")
    @Operation(
            summary = "分页查询订单列表"
    )
    public Response getOrderList(@RequestParam(required = false) String performanceTitle,
                                 @RequestParam(required = false) int pageNum) {
        return Response.success(200, "查询订单列表成功", adminOrderService.getOrderList(performanceTitle, pageNum));
    }


    @GetMapping("/api/admin/order/{performanceId}")
    @Operation(
            summary = "查询订单详情"
    )
    public Response getOrderDetails(@PathVariable Long performanceId) {
        return Response.success(200, "查询订单详情成功", adminOrderService.getOrderDetails(performanceId));
    }

    @PostMapping("/api/admin/order/update")
    @Operation(
            summary = "修改订单信息（手动更新订单状态）"
    )
    public Response updateOrder(@RequestBody TicketOrder ticketOrder) {
        adminOrderService.updateOrder(ticketOrder);
        return Response.success(200, "修改订单信息成功");
    }

}
