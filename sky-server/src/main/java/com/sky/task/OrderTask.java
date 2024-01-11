package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sky.entity.Orders;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderService orderService;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ? ") //每分钟触发一次
    public void processTimeoutOrder() {
        log.info("处理支付超时订单");
        LambdaUpdateWrapper<Orders> luw = new LambdaUpdateWrapper<>();
        luw.eq(Orders::getStatus, Orders.PENDING_PAYMENT);
        luw.le(Orders::getOrderTime, LocalDateTime.now().minusMinutes(15L));
        luw.set(Orders::getStatus, Orders.CANCELLED)
                .set(Orders::getCancelReason, Orders.CANCEL_REASON_OVERTIME)
                .set(Orders::getCancelTime, LocalDateTime.now());
        orderService.update(luw);
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ? ")//每天凌晨1店触发一次
    public void processDeliveryOrder() {
        log.info("处理一直处于派送中的订单");
        LambdaUpdateWrapper<Orders> luw = new LambdaUpdateWrapper<>();
        luw.eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS);
        luw.le(Orders::getOrderTime, LocalDateTime.now().minusMinutes(60L));
        luw.set(Orders::getStatus, Orders.COMPLETED);
        orderService.update(luw);
    }
}
