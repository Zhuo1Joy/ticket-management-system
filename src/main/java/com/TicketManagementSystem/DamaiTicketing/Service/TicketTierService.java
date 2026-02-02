package com.TicketManagementSystem.DamaiTicketing.Service;
import com.TicketManagementSystem.DamaiTicketing.Entity.PerformanceSession;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.TicketTierMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketTierService extends ServiceImpl<TicketTierMapper, TicketTier> {

    // 获取演出票档信息
    public List<TicketTier> getTicketTier(Long performanceSessionId) {
        List<TicketTier> result =  this.lambdaQuery()
                .eq(TicketTier::getSessionId, performanceSessionId)
                .list();
        if (result.isEmpty()) throw new BusinessException(404, "暂无相关票档信息");
        return result;
    }

    // 用户获取是否有库存
    public void isTicketAvailable(Long performanceSessionId){
        TicketTier result = this.lambdaQuery()
                .eq(TicketTier::getSessionId, performanceSessionId)
                .eq(TicketTier::getIsAvailable, 0)
                .one();
        if (result != null) throw new BusinessException(401, "该档次票已售空");
    }

    // 添加票档信息
    public void setTier(TicketTier ticketTier) {
        TicketTier tier = new TicketTier();
        tier.setSessionId(ticketTier.getSessionId());
        tier.setTierName(ticketTier.getTierName());
        tier.setPrice(ticketTier.getPrice());
        tier.setTotalQuantity(ticketTier.getTotalQuantity());
        tier.setAvailableQuantity(ticketTier.getAvailableQuantity());

        boolean result = save(tier);
        if (!result) throw new RuntimeException("添加票档失败");
    }

    // 更新票档信息
    public void updateTier(TicketTier tier) {
        boolean result = this.lambdaUpdate()
                .eq(TicketTier::getId, tier.getId())
                .set(tier.getSessionId() != null, TicketTier::getSessionId, tier.getSessionId())
                .set(tier.getTierName() != null, TicketTier::getTierName, tier.getTierName())
                .set(tier.getPrice() != null, TicketTier::getPrice, tier.getPrice())
                .set(tier.getTotalQuantity() != null, TicketTier::getTotalQuantity, tier.getTotalQuantity())
                .set(tier.getAvailableQuantity() != null, TicketTier::getAvailableQuantity, tier.getAvailableQuantity())
                .set(tier.getIsAvailable() != null, TicketTier::getIsAvailable, tier.getIsAvailable())
                .update();

        if (!result) throw new RuntimeException("更新票档失败");
    }

    // 删除票档信息
    public void deleteTier(List<PerformanceSession> sessions) {

        List<Long> sessionIds = sessions.stream()
                .map(PerformanceSession::getId)
                .toList();

        boolean result = this.lambdaUpdate()
                .in(TicketTier::getSessionId, sessionIds)
                .remove();
        if (!result) throw new RuntimeException("删除票档信息失败");
    }

}
