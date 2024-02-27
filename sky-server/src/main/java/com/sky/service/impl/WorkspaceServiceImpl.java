package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.mapper.*;
import com.sky.service.*;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl extends ServiceImpl<WorkspaceMapper, Orders> implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private OrderService orderService;

    /**
     * 根据时间段统计营业数据
     *
     * @param begin
     * @param end
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        /**
         * 营业额：当日已完成订单的总金额
         * 有效订单：当日已完成订单的数量
         * 订单完成率：有效订单数 / 总订单数
         * 平均客单价：营业额 / 有效订单数
         * 新增用户：当日新增用户的数量
         */

        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);

        //查询总订单数
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        long totalOrderCount = orderService.count();
        //有效订单数
        lqw.eq(Orders::getStatus, Orders.COMPLETED);
        int validOrderCount = (int) orderService.count(lqw);
        //营业额
        double turnover = orderService.getBaseMapper().selectList(lqw).stream().map(Orders::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();

        Double unitPrice = 0.0;

        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0 && validOrderCount != 0) {
            //订单完成率
            orderCompletionRate = (double) validOrderCount / (double) totalOrderCount;
            //平均客单价
            unitPrice = turnover / validOrderCount;
        }

        //新增用户数
        LambdaQueryWrapper<User> lqw2 = new LambdaQueryWrapper<>();
        lqw2.between(User::getCreateTime, begin, end);
        int newUsers = (int) userService.count(lqw2);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView() {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        Map<Integer, Integer> orderCountMap = new HashMap<>();
        queryWrapper.select("status", "count(*) as count")
                .groupBy("status");
        List<Map<String, Object>> maps = orderService.listMaps(queryWrapper);
        maps.forEach(map -> {
            orderCountMap.put((Integer) map.get("status"), ((Long) map.get("count")).intValue());
        });
        Integer waitingOrders = orderCountMap.get(Orders.TO_BE_CONFIRMED);
        Integer deliveredOrders = orderCountMap.get(Orders.CONFIRMED);
        Integer completedOrders = orderCountMap.get(Orders.COMPLETED);
        Integer cancelledOrders = orderCountMap.get(Orders.CANCELLED);
        Integer allOrders = (int) orderService.count();
        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    public DishOverViewVO getDishOverView() {
        LambdaQueryWrapper<Dish> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(Dish::getStatus, StatusConstant.ENABLE);
        Integer sold = (int) dishService.count(lqw1);

        LambdaQueryWrapper<Dish> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(Dish::getStatus, StatusConstant.DISABLE);
        Integer discontinued = (int) dishService.count(lqw2);
        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    public SetmealOverViewVO getSetmealOverView() {
        LambdaQueryWrapper<Setmeal> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(Setmeal::getStatus, StatusConstant.ENABLE);
        Integer sold = (int)setmealService.count(lqw1);

        LambdaQueryWrapper<Setmeal> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(Setmeal::getStatus, StatusConstant.DISABLE);
        Integer discontinued = (int)setmealService.count(lqw2);
        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
