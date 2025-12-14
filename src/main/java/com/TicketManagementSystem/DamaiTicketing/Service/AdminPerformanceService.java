package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.Performance;
import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceSession;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminPerformanceService {

    @Autowired
    PerformanceService performanceService;

    @Autowired
    PerformanceSessionService performanceSessionService;

    @Autowired
    TicketTierService ticketTierService;

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
    public void setTier(TicketTier ticketTier) {
        ticketTierService.setTier(ticketTier);
    }

    // 更新票档信息
    public void updateTier(TicketTier tier) {
        ticketTierService.updateTier(tier);
    }

    // 按参数查询->城市、分类
    public Page<Performance> selectPerformanceByParams(String city, String category, int pageNum) {
        return performanceService.selectPerformanceByParams(city, category, pageNum);
    }

    // 获取演出详情
    public Performance getPerformanceDetails(Long performanceId) {
        return performanceService.getPerformanceDetails(performanceId);
    }

    // 删除演出信息（连着场次和票档一起删除）
    public void deletePerformance(Long performanceId) {
        performanceService.deletePerformance(performanceId);
        performanceSessionService.deleteSession(performanceId);
        // 好恶心。
        ticketTierService.deleteTier(performanceSessionService.getSession(performanceId).get(0).getPerformanceId());
    }

}
