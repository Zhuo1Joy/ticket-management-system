package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.TicketOrder;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.TicketOrderMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminOrderService extends ServiceImpl<TicketOrderMapper, TicketOrder> {

    // 查询订单列表
    public Page<String> getOrderList(String performanceTitle, int pageNum) {

        int currentPage = Math.max(pageNum, 1);
        Page<TicketOrder> page = new Page<>(currentPage, 20);

        Page<TicketOrder> orders = this.lambdaQuery()
                .like(performanceTitle != null, TicketOrder::getOrderName, performanceTitle)
                .page(page);

        if (orders.getRecords().isEmpty()) throw new BusinessException(404, "没有可查询的订单");

        List<String> result = orders.getRecords()
                .stream()
                .map(TicketOrder::getOrderName)
                .toList();

        Page<String> pageName = new Page<>(currentPage, 20);
        pageName.setTotal(orders.getTotal());
        pageName.setRecords(result);

        if (currentPage > pageName.getPages()) throw new BusinessException(404, "请选择正确的页码");
        return pageName;

    }

    // 获取订单详情
    public TicketOrder getOrderDetails(Long orderId) {

        TicketOrder result = this.lambdaQuery()
                .eq(TicketOrder::getId, orderId)
                .one();

        if (result == null) throw new BusinessException(404, "您查询的订单不存在");
        return result;

    }

    // 修改订单信息（手动更新订单状态）
    // 目前只写了更新状态 需要可以再加
    public void updateOrder(TicketOrder ticketOrder) {
        boolean result = this.lambdaUpdate()
                .eq(TicketOrder::getId, ticketOrder.getId())
                .set(TicketOrder::getStatus, ticketOrder.getStatus())
                .update();
        if (!result) throw new RuntimeException("修改订单信息失败");
    }

}
