package com.TicketManagementSystem.DamaiTicketing.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.GrabTicketRequest;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketOrder;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.PerformanceMapper;
import com.TicketManagementSystem.DamaiTicketing.Mapper.TicketOrderMapper;
import com.TicketManagementSystem.DamaiTicketing.Mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TicketOrderService extends ServiceImpl<TicketOrderMapper, TicketOrder> {

    @Autowired
    TicketTierService ticketTierService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    PerformanceMapper performanceMapper;

    // 查询订单列表
    public List<String> getOrderList(String performanceTitle) {

        List<TicketOrder> orders = this.lambdaQuery()
                .eq(TicketOrder::getUserId, StpUtil.getLoginIdAsLong())
                .like(performanceTitle != null, TicketOrder::getOrderName, performanceTitle)
                .list();

        List<String> result = orders.stream()
                .map(TicketOrder::getOrderName)
                .toList();

        if (result.isEmpty()) throw new BusinessException(404, "暂无相关订单");
        return result;

    }

    // 获取订单详情
    // TODO 这里的订单 在付款前和后看到的应该是两个状态 等等 好像不用？
    public TicketOrder getOrderDetails(Long id) {

        TicketOrder result = this.lambdaQuery()
                .eq(TicketOrder::getUserId, StpUtil.getLoginIdAsLong()) // 我在想真正的界面这里用户应该看不到不是自己的订单 那我是不是没必要加上判断订单是否属于该用户
                .eq(TicketOrder::getId, id)
                .one();

        if (result == null) throw new BusinessException(404, "您查询的订单不存在");
        return result;

    }

    // 这里的删除订单和删除订单历史记录不是一个东西
    // 这里做的应该是删除未支付的订单 也就是取消订单这个东西
    // TODO 等以后我再允许你退货 现在不允许 因为我不会写
    public void deleteOrder(Long id) {

        // 票档ID
        Long tierId = lambdaQuery()
                .eq(TicketOrder::getId, id)
                .one()
                .getTierId();

        // 购买票数
        int quantity = this.lambdaQuery()
                .eq(TicketOrder::getId, id)
                .one()
                .getQuantity();

        boolean result = this.lambdaUpdate()
                .eq(TicketOrder::getUserId, StpUtil.getLoginIdAsLong())
                .eq(TicketOrder::getId, id)
                .eq(TicketOrder::getStatus, 0)
                .remove();

        if (!result) throw new BusinessException(401, "无法删除该订单");

        // 对应的我要更新票档的剩余量
        ticketTierService.lambdaUpdate()
                .eq(TicketTier::getId, tierId)
                .setSql("available_quantity = available_quantity + " + quantity)
                .update();

    }

    // 创建新订单
    public TicketOrder createOrder(GrabTicketRequest grabTicketRequest, BigDecimal amount) {

        // 创建订单号 被推荐了时间戳+随机数
        long timestamp = System.currentTimeMillis();
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
        // TODO 在这里订单号不能保证百分百唯一 后期或许可以补一个雪花算法？

        TicketOrder order = new TicketOrder();
        order.setOrderNo("M"+timestamp+randomNum);
        order.setOrderName(userMapper.selectById(StpUtil.getLoginIdAsLong()).getNickname() + "的"
                + performanceMapper.selectById(grabTicketRequest.getPerformanceId()).getTitle() + "订单");
        order.setUserId(StpUtil.getLoginIdAsLong());
        order.setPerformanceId(grabTicketRequest.getPerformanceId());
        order.setSessionId(grabTicketRequest.getSessionId());
        order.setTierId(grabTicketRequest.getTierId());
        order.setQuantity(grabTicketRequest.getQuantity());
        order.setTotalAmount(amount);
        save(order);

        return order;

    }

    // TODO 这里还需要一个到时关闭订单 也就是改变订单状态的自动方法

}
