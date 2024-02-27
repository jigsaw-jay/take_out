package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.result.Result;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    Result<TurnoverReportVO> turnoverStatistics(LocalDate begin, LocalDate end);

    Result<UserReportVO> userStatistics(LocalDate begin, LocalDate end);

    Result<OrderReportVO> ordersStatistics(LocalDate begin, LocalDate end);

    Result<SalesTop10ReportVO> top10(LocalDate begin, LocalDate end);
}
