package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.TicketManagementSystem.DamaiTicketing.Entity.*;
import com.TicketManagementSystem.DamaiTicketing.Service.AdminPerformanceService;
import com.TicketManagementSystem.DamaiTicketing.Util.StpAdminUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "演出管理", description = "管理员操作演出相关的所有接口")
@SaCheckPermission(value = "admin", type = StpAdminUtil.TYPE)
public class AdminPerformanceController {

    final
    AdminPerformanceService adminPerformanceService;

    public AdminPerformanceController(AdminPerformanceService adminPerformanceService) {
        this.adminPerformanceService = adminPerformanceService;
    }

    @PostMapping("/api/admin/performance")
    @Operation(
            summary = "添加演出信息"
    )
    public Response setPerformance(@RequestBody PerformanceRequest performanceRequest) {
        adminPerformanceService.setPerformance(performanceRequest.getPerformance());
        adminPerformanceService.setSession(performanceRequest.getPerformanceSession());
        adminPerformanceService.setTier(performanceRequest.getTicketTier());
        return Response.success(200, "添加演出信息成功");
    }


    @PutMapping("/api/admin/performance/update")
    @Operation(
            summary = "修改演出信息"
    )
    public Response updatePerformance(@RequestBody(required = false) PerformanceRequest performanceRequest) {
        if (performanceRequest.getPerformance() != null) adminPerformanceService.updatePerformance(performanceRequest.getPerformance());
        if (performanceRequest.getPerformanceSession() != null) adminPerformanceService.updateSession(performanceRequest.getPerformanceSession());
        if (performanceRequest.getTicketTier() != null) adminPerformanceService.updateTier(performanceRequest.getTicketTier());
        return Response.success(200, "修改演出信息成功");
    }


    @GetMapping("/api/admin/performance/select")
    @Operation(
            summary = "分页查询演出信息"
    )
    public Response selectPerformanceByParams(@RequestParam(required = false) String city,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false, defaultValue = "1") int pageNum) {
        return Response.success(200, "查询演出信息成功", adminPerformanceService.selectPerformanceByParams(city, category, pageNum));
    }

    @GetMapping("/api/admin/performance/details")
    @Operation(
            summary = "获取演出详情"
    )
    public Response getPerformanceDetails(Long performanceId) {
        return Response.success(200, "获取该演出详情成功", adminPerformanceService.getPerformanceDetails(performanceId));
    }


    @DeleteMapping("/api/admin/performance/delete/{performanceId}")
    @Operation(
            summary = "删除演出信息"
    )
    public Response deletePerformance(@PathVariable Long performanceId) {
        adminPerformanceService.deletePerformance(performanceId);
        return Response.success(200, "删除演出信息成功");
    }

}
