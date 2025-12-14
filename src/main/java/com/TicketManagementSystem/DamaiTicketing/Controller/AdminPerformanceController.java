package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.TicketManagementSystem.DamaiTicketing.Entity.Performance;
import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceSession;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import com.TicketManagementSystem.DamaiTicketing.Service.AdminPerformanceService;
import com.TicketManagementSystem.DamaiTicketing.Util.StpAdminUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "演出管理", description = "管理员操作演出相关的所有接口")
public class AdminPerformanceController {

    @Autowired
    AdminPerformanceService adminPerformanceService;

    @GetMapping("/api/admin/performance")
    @SaCheckLogin(type = StpAdminUtil.TYPE)
    @Operation(
            summary = "添加演出信息"
    )
    public Response setPerformance(@RequestBody Performance performance,
                                   @RequestBody PerformanceSession performanceSession,
                                   @RequestBody TicketTier ticketTier) {
        adminPerformanceService.setPerformance(performance);
        adminPerformanceService.setSession(performanceSession);
        adminPerformanceService.setTier(ticketTier);
        return Response.success(200, "添加演出信息成功");
    }


    @PostMapping("/api/admin/performance/update")
    @SaCheckLogin(type = StpAdminUtil.TYPE)
    @Operation(
            summary = "修改演出信息"
    )
    public Response updatePerformance(@RequestBody(required = false) Performance performance,
                                      @RequestBody(required = false) PerformanceSession performanceSession,
                                      @RequestBody(required = false) TicketTier ticketTier) {
        adminPerformanceService.updatePerformance(performance);
        adminPerformanceService.updateSession(performanceSession);
        adminPerformanceService.updateTier(ticketTier);
        return Response.success(200, "修改演出信息成功");
    }


    @GetMapping("/api/admin/performance/select")
    @SaCheckLogin(type = StpAdminUtil.TYPE)
    @Operation(
            summary = "分页查询演出信息"
    )
    public Response selectPerformanceByParams(@RequestParam(required = false) String city,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false) int pageNum) {
        return Response.success(200, "查询演出信息成功", adminPerformanceService.selectPerformanceByParams(city, category, pageNum));
    }

    @GetMapping("/api/admin/performance/details")
    @SaCheckLogin(type = StpAdminUtil.TYPE)
    @Operation(
            summary = "获取演出详情"
    )
    public Response getPerformanceDetails(Long performanceId) {
        return Response.success(200, "获取演出详情成功", adminPerformanceService.getPerformanceDetails(performanceId));
    }


    @DeleteMapping("/api/admin/performance/delete/{performanceId}")
    @SaCheckLogin(type = StpAdminUtil.TYPE)
    @Operation(
            summary = "删除演出信息"
    )
    public Response deletePerformance(@PathVariable Long performanceId) {
        adminPerformanceService.deletePerformance(performanceId);
        return Response.success(200, "删除演出信息成功");
    }


}
