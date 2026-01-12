package com.TicketManagementSystem.DamaiTicketing.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.GrabTicketRequest;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketOrder;
import com.TicketManagementSystem.DamaiTicketing.Entity.TicketTier;
import com.TicketManagementSystem.DamaiTicketing.Enums.OrderStatus;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.PerformanceMapper;
import com.TicketManagementSystem.DamaiTicketing.Mapper.TicketOrderMapper;
import com.TicketManagementSystem.DamaiTicketing.Mapper.UserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class TicketOrderService extends ServiceImpl<TicketOrderMapper, TicketOrder> {

    final
    TicketTierService ticketTierService;

    final
    UserMapper userMapper;

    final
    PerformanceMapper performanceMapper;

    final
    UserService userService;

    public TicketOrderService(TicketTierService ticketTierService, UserMapper userMapper, PerformanceMapper performanceMapper, UserService userService) {
        this.ticketTierService = ticketTierService;
        this.userMapper = userMapper;
        this.performanceMapper = performanceMapper;
        this.userService = userService;
    }

    // 查询订单列表
    public Page<String> getOrderList(String performanceTitle, int pageNum) {

        int currentPage = Math.max(pageNum, 1);
        Page<TicketOrder> page = new Page<>(currentPage, 20);

        Page<TicketOrder> orders = this.lambdaQuery()
                .eq(TicketOrder::getUserId, StpUtil.getLoginIdAsLong())
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
    // TODO 这里的订单 在付款前和后看到的应该是两个状态 等等 好像不用？
    public TicketOrder getOrderDetails(Long id) {

        TicketOrder result = this.lambdaQuery()
                .eq(TicketOrder::getUserId, StpUtil.getLoginIdAsLong()) // 我在想真正的界面这里用户应该看不到不是自己的订单 那我是不是没必要加上判断订单是否属于该用户
                .eq(TicketOrder::getId, id)
                .one();

        if (result == null) throw new BusinessException(404, "您查询的订单不存在");
        return result;

    }

    // 取消订单
    // TODO 等以后我再允许你退货 现在不允许 因为我不会写
    public void cancelOrder(Long id, String orderNo, String cancelReason) {

        boolean result = lambdaUpdate()
                .eq(id != null, TicketOrder::getId, id)
                .or()
                .eq(orderNo != null, TicketOrder::getOrderNo, orderNo)
                .eq(TicketOrder::getStatus, OrderStatus.fromCode(0))
                .set(TicketOrder::getStatus, OrderStatus.fromCode(2))
                .set(TicketOrder::getCancelTime, LocalDateTime.now())
                .set(cancelReason != null, TicketOrder::getCancelReason, cancelReason)
                .update();

        if (!result) throw new BusinessException(401, "无法取消该订单");

        // 票档 ID
        Long tierId = lambdaQuery()
                .eq(TicketOrder::getId, id)
                .or()
                .eq(TicketOrder::getOrderNo, orderNo)
                .one()
                .getTierId();

        // 购买票数
        int quantity = this.lambdaQuery()
                .eq(TicketOrder::getId, id)
                .or()
                .eq(TicketOrder::getOrderNo, orderNo)
                .one()
                .getQuantity();

        // 对应的我要更新票档的剩余量
        ticketTierService.lambdaUpdate()
                .eq(TicketTier::getId, tierId)
                .setSql("available_quantity = available_quantity + " + quantity)
                .update();

    }

    // 这里做的应该是删除已完成/已过期/已取消的订单
    public void deleteOrder(Long id) {

        boolean result = this.lambdaUpdate()
                .eq(TicketOrder::getUserId, StpUtil.getLoginIdAsLong())
                .eq(TicketOrder::getId, id)
                .in(TicketOrder::getStatus, OrderStatus.fromCode(1), OrderStatus.fromCode(2))
                .remove();

        if (!result) throw new BusinessException(401, "无法删除该订单");

    }

    // 创建新订单
    public TicketOrder createOrder(GrabTicketRequest grabTicketRequest, Long userId, BigDecimal amount) {

        // 创建订单号 被推荐了时间戳+随机数
        long timestamp = System.currentTimeMillis();
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
        // TODO 在这里订单号不能保证百分百唯一 后期或许可以补一个雪花算法？

        // 用户邮箱
        String userEmail = userService.getUserEmail(userId);

        TicketOrder order = new TicketOrder();
        order.setOrderNo("M"+timestamp+randomNum);
        order.setOrderName(userMapper.selectById(userId).getNickname() + "的"
                + performanceMapper.selectById(grabTicketRequest.getPerformanceId()).getTitle() + "订单");
        order.setUserId(userId);
        order.setUserEmail(userEmail);
        order.setPerformanceId(grabTicketRequest.getPerformanceId());
        order.setSessionId(grabTicketRequest.getSessionId());
        order.setTierId(grabTicketRequest.getTierId());
        order.setQuantity(grabTicketRequest.getQuantity());
        order.setTotalAmount(amount);
        save(order);

        return order;

    }

    // 更新订单状态为成功
    public void updateSuccessOrder(String orderNo) {
        boolean result = this.lambdaUpdate()
                .eq(TicketOrder::getOrderNo, orderNo)
                .set(TicketOrder::getStatus, OrderStatus.fromCode(1))
                .set(TicketOrder::getPaymentTime, LocalDateTime.now())
                .update();

        if (!result) {
            log.error("无法更新该订单");
        }
        log.info("更新订单状态成功：{}", orderNo);
    }

    // 通过订单号获取用户邮箱
    public String getEmailByOrderNo (String orderNo) {
        return this.lambdaQuery()
                .eq(TicketOrder::getOrderNo, orderNo)
                .one()
                .getUserEmail();
    }

    // 根据订单 ID查询订单
    public TicketOrder selectByOrderId(Long orderId) {
        TicketOrder result = this.lambdaQuery()
                .eq(TicketOrder::getUserId, StpUtil.getLoginIdAsLong())
                .eq(TicketOrder::getId, orderId)
                .one();
        if (result == null) throw new BusinessException(404, "您查询的订单不存在");
        return result;
    }

}
