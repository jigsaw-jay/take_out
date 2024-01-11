package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.OrderPaymentVO;
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
}
