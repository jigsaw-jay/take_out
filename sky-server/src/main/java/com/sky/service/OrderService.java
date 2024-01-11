package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService extends IService<Orders> {
    /**
     * 提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    Result<OrderSubmitVO> submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    Result<OrderPaymentVO> payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 修改订单状态
     *
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    Result<OrderVO> queryOrderDetail(Long id);

    /**
     * 历史订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Result<PageResult> ordersPage(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 取消订单
     * @param id
     * @return
     */
    Result cancelOrder(Long id);

    /**
     * 再来一单
     * @param id
     * @return
     */
    Result reSubmitorder(Long id);

    /**
     * 订单分页搜索
     * @param ordersPageQueryDTO
     * @return
     */
    Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    Result<OrderStatisticsVO> statistics();

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    Result confirmOrder(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    Result rejectOrder(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 派送订单
     * @param id
     * @return
     */
    Result deliverOrder(Long id);

    /**
     * 完成订单
     * @param id
     * @return
     */
    Result completeOrder(Long id);
}
