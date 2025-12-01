package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.Performance;
import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceSession;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.PerformanceMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerformanceService extends ServiceImpl<PerformanceMapper, Performance> {

    @Autowired
    PerformanceSessionService performanceSessionService;
    @Autowired
    TicketTierService ticketTierService;

    // 未登录默认返回北京地区演出
    public List<Performance> getPerformance() {
        List<Performance> result =  this.lambdaQuery()
                .eq(Performance::getCity, "北京")
                .list();
        if (result.isEmpty()) throw new BusinessException(404, "暂无相关演出");
        return result;
    }

    // 根据演出名/明星名查询
    public List<Performance> selectPerformanceByMessage(String keyword) {
        List<Performance> result = this.lambdaQuery()
                .like(Performance::getTitle, keyword)
                .or()
                .like(Performance::getCelebrity, keyword)
                .list();
        if (result.isEmpty()) throw new BusinessException(404, "暂无相关演出");
        return result;
    }

    // 按参数查询->城市、分类
    public List<Performance> selectPerformanceByParams(String city, String category) {
        List<Performance> result = this.lambdaQuery()
                .eq(city != null, Performance::getCity, city)
                .eq(category != null, Performance::getCategory, category)
                .list();
        if (result.isEmpty()) throw new BusinessException(404, "暂无相关演出");
        return result;
    }

    // 获取演出场次信息
    public List<PerformanceSession> getSession(Long performanceId) {
        return performanceSessionService.getSession(performanceId);
    }

    // 获取演出票务信息
    public List<TicketTier> getTicketTier(Long performanceSessionId) {
        return ticketTierService.getTicketTier(performanceSessionId);
    }

    // 用户获取是否有库存
    public void isTicketAvailable(Long performanceSessionId){
        ticketTierService.isTicketAvailable(performanceSessionId);
    }

    // 是否已经开票
    public void isSessionOnSale(Long performanceId) {
        performanceSessionService.isSessionOnSale(performanceId);
    }

}
