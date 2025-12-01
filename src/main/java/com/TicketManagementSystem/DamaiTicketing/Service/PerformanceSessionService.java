package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceSession;
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

}
