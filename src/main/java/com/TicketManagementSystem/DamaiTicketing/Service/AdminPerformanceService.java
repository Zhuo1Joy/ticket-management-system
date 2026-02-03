package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.Performance;
import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceDetail;
import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceSession;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class AdminPerformanceService {

    final
    PerformanceService performanceService;

    final
    PerformanceSessionService performanceSessionService;

    final
    TicketTierService ticketTierService;

    public AdminPerformanceService(PerformanceService performanceService, PerformanceSessionService performanceSessionService, TicketTierService ticketTierService) {
        this.performanceService = performanceService;
        this.performanceSessionService = performanceSessionService;
        this.ticketTierService = ticketTierService;
    }

    // 添加演出信息
    public void setPerformance(Performance performance) {
        performanceService.setPerformance(performance);
    }

    // 更新演出信息
    public void updatePerformance(Performance performance) {
        performanceService.updatePerformance(performance);
    }

    // 添加演出场次信息
    public void setSession(PerformanceSession session) {
        performanceSessionService.setSession(session);
    }

    // 更新场次信息
    public void updateSession(PerformanceSession session) {
        performanceSessionService.updateSession(session);
    }

    // 添加演出票档信息
    public void setTier(TicketTier ticketTier) { ticketTierService.setTier(ticketTier);}

    // 更新票档信息
    public void updateTier(TicketTier tier) {
        ticketTierService.updateTier(tier);
    }

    // 按参数查询->城市、分类
    public Page<Performance> selectPerformanceByParams(String city, String category, int pageNum) {
        return performanceService.selectPerformanceByParams(city, category, pageNum);
    }

    public List<PerformanceDetail> getPerformanceDetails(Long performanceId) {
        List<PerformanceDetail> result = new LinkedList<>();
        // 分别列出场次和票档
        List<PerformanceSession> sessions = performanceSessionService.getSession(performanceId);
        for (PerformanceSession session : sessions) {
            PerformanceDetail detail = new PerformanceDetail();
            detail.setPerformanceSession(session);
            detail.setTicketTiers(ticketTierService.getTicketTierBySession(session.getId()));
            // 存入
            result.add(detail);
        }
        return result;
    }

    // 删除演出信息（连着场次和票档一起删除）
    @Transactional
    public void deletePerformance(Long performanceId) {
        performanceService.deletePerformance(performanceId);
        performanceSessionService.deleteSession(performanceId);
        ticketTierService.deleteTier(performanceId);
    }

}
