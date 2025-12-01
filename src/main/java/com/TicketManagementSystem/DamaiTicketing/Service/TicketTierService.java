package com.TicketManagementSystem.DamaiTicketing.Service;

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

}
