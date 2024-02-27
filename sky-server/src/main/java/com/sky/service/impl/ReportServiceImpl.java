package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.result.Result;
import com.sky.service.OrderDetailService;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.service.UserService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 统计时间内营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public Result<TurnoverReportVO> turnoverStatistics(LocalDate begin, LocalDate end) {
        List<Double> turnoverList = new ArrayList<>();//存放营业额
        List<LocalDate> dateList = getDate(begin, end);
        for (LocalDate date : dateList) {
            //查询营业额(当日已完成订单)
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Orders::getStatus, Orders.COMPLETED)
                    .between(begin != null && end != null, Orders::getOrderTime, beginTime, endTime);
            double turnover = BigDecimal.valueOf(orderService.getBaseMapper()
                    .selectList(lqw).stream()
                    .map(Orders::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue()).doubleValue();
            turnoverList.add(turnover);
        }
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
        return Result.success(turnoverReportVO);
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public Result<UserReportVO> userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDate(begin, end);
        List<Long> totalList = new ArrayList<>();
        List<Long> newList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.le(User::getCreateTime, endTime);
            long totalUser = userService.count(lqw);
            totalList.add(totalUser);
            lqw.gt(User::getCreateTime, beginTime);
            long newUser = userService.count(lqw);
            newList.add(newUser);
        }
        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalList, ","))
                .newUserList(StringUtils.join(newList, ","))
                .build();
        return Result.success(userReportVO);
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public Result<OrderReportVO> ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDate(begin, end);
        List<Long> orderCountList = new ArrayList<>();
        List<Long> validOrderCountList = new ArrayList<>();
        int totalOrderCount = 0;
        int validOrderCount = 0;
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
            lqw.le(Orders::getOrderTime, endTime);
            lqw.gt(Orders::getOrderTime, beginTime);
            long order = orderService.count(lqw);
            totalOrderCount += order;
            orderCountList.add(order);
            lqw.eq(Orders::getStatus, Orders.COMPLETED);
            long validOrder = orderService.count(lqw);
            validOrderCount += validOrder;
            validOrderCountList.add(validOrder);
        }
        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate((double) validOrderCount / totalOrderCount)
                .build();
        return Result.success(orderReportVO);
    }


    /**
     * 销量top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public Result<SalesTop10ReportVO> top10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> Top10 = orderMapper.getSalesTop(beginTime, endTime);
        List<String> name = Top10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> number = Top10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .numberList(StringUtils.join(number, ","))
                .nameList(StringUtils.join(name, ","))
                .build();
        return Result.success(salesTop10ReportVO);
    }


    public List<LocalDate> getDate(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();//存放区间内的每天
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
}
