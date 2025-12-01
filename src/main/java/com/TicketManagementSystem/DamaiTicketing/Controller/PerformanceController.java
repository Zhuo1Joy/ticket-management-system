package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.TicketManagementSystem.DamaiTicketing.Entity.GrabTicketRequest;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Service.PerformanceService;
import com.TicketManagementSystem.DamaiTicketing.Service.TicketGrabbingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "演出模块", description = "演出相关的所有操作接口")
public class PerformanceController {

    @Autowired
    PerformanceService performanceService;

    @Autowired
    TicketGrabbingService ticketGrabbingService;

    // 未登录默认返回北京地区演出
    @GetMapping("/api/performance/home")
    @Operation(
            summary = "未登录默认返回北京地区演出"
    )
    public Response getPerformance() {
        return Response.success(200, "默认地区：北京", performanceService.getPerformance());
    }

    // 根据演出名/明星名查询
    @GetMapping("/api/performance/search/{keyword}")
    @Operation(
            summary = "根据演出名/明星名查询"
    )
    public Response selectPerformanceByMessage(@PathVariable String keyword) {
        return Response.success(200, "查询成功", performanceService.selectPerformanceByMessage(keyword));
    }

    // 按参数查询->城市、分类
    @GetMapping("/api/performance")
    @Operation(
            summary = "按参数查询->城市、分类"
    )
    public Response selectPerformanceByParams(@RequestParam(required = false) String city, @RequestParam(required = false) String category) {
        return Response.success(200, "查询成功", performanceService.selectPerformanceByParams(city, category));
    }

    // 获取演出场次信息
    @SaCheckLogin // 登录才能看
    @GetMapping("/api/performance/{performanceId}")
    @Operation(
            summary = "获取演出场次信息"
    )
    public Response selectSession(@PathVariable Long performanceId) {
        return Response.success(200, "查询成功", performanceService.getSession(performanceId));
    }

    // 获取演出票档信息
    @SaCheckLogin
    @GetMapping("/api/performance/{sessionId}/ticket-tier")
    @Operation(
            summary = "获取演出票档信息"
    )
    public Response selectTicketTier(@PathVariable Long sessionId) {
        return Response.success(200, "查询成功", performanceService.getTicketTier(sessionId));
    }

    // 用户获取是否有库存
    @SaCheckLogin
    @GetMapping("/api/ticket/{performanceId}")
    @Operation(
            summary = "用户获取是否有库存"
    )
    public Response isTicketAvailable(@PathVariable Long performanceId) {
        performanceService.isTicketAvailable(performanceId);
        return Response.success(200, "还有票！快抢");
    }

    // 是否已经开票
    @SaCheckLogin
    @GetMapping("/api/perform/status/{sessionId}")
    @Operation(
            summary = "是否已经开票"
    )
    public Response isOnSale(@PathVariable Long sessionId) {
        performanceService.isSessionOnSale(sessionId);
        return Response.success(200, "已开票");
    }

    // 抢票
    @SaCheckLogin
    @PostMapping("/api/ticket/grab")
    @Operation(
            summary =  "抢票"
    )
    public Response ticketGrab(@RequestBody GrabTicketRequest grabTicketRequest){
        return Response.success(200, "抢票成功", ticketGrabbingService.grabTicket(grabTicketRequest));
    }

}
