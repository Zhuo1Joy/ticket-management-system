package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceSession;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.PerformanceSessionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerformanceSessionService extends ServiceImpl<PerformanceSessionMapper, PerformanceSession> {

    // 获取演出场次信息
    public List<PerformanceSession> getSession(Long performanceId) {
        List<PerformanceSession> result =  this.lambdaQuery()
                .eq(PerformanceSession::getPerformanceId, performanceId)
                .list();
        if (result.isEmpty()) throw new BusinessException(404, "暂无相关场次");
        return result;
    }

    // 是否已经开票
    public void isSessionOnSale(Long performanceId) {
        PerformanceSession result = this.lambdaQuery()
                .eq(PerformanceSession::getPerformanceId, performanceId)
                .eq(PerformanceSession::getIsOnSale, 0)
                .one();
        System.out.println(result);
        if (result != null) throw new BusinessException(401, "未开票");
    }

    // 添加演唱场次
    public void setSession(PerformanceSession session) {

        PerformanceSession performanceSession = new PerformanceSession();
        performanceSession.setPerformanceId(session.getPerformanceId());
        performanceSession.setSessionTime(session.getSessionTime());
        performanceSession.setVenue(session.getVenue());
        performanceSession.setAddress(session.getAddress());

        boolean result = save(performanceSession);
        if (!result) throw new RuntimeException("添加演出场次失败");

    }

    // 更新场次信息
    public void updateSession(PerformanceSession session) {
        boolean result = this.lambdaUpdate()
                .eq(PerformanceSession::getPerformanceId, session.getPerformanceId())
                .set(session.getSessionTime() != null, PerformanceSession::getSessionTime, session.getSessionTime())
                .set(session.getVenue() != null, PerformanceSession::getVenue, session.getVenue())
                .set(session.getAddress() != null, PerformanceSession::getAddress,session.getAddress())
                .update();

        if (!result) throw new RuntimeException("更新场次信息失败");
    }

    // 删除场次信息
    public void deleteSession(Long performanceId) {
        boolean result = this.lambdaUpdate()
                .eq(PerformanceSession::getPerformanceId, performanceId)
                .remove();
        if (!result) throw new RuntimeException("删除场次信息失败");
    }

}
