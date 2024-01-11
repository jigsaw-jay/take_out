package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.*;
import com.sky.utils.RedisUtils;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.DishVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sky.constant.RedisConstants.CACHE_SHOPINGCART_TTL;
import static com.sky.constant.RedisConstants.CAHCE_SHOPINGCART_KEY;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private UserService userService;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public Result<OrderSubmitVO> submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务异常(地址簿为空、购物车为空）
        AddressBook addressBook = addressBookService.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(lqw);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向orders插入一条数据
        Orders orders = new Orders();
        BeanUtil.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID); //未支付
        orders.setStatus(Orders.PENDING_PAYMENT); //待付款
        orders.setNumber(String.valueOf(redisUtils.nextId("order")));//Redis生成全局唯一Id
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        save(orders);
        //向order_detail表插入n条数据
        log.info("orderId:{}", orders.getId());
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(shoppingCart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtil.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());
        orderDetailService.saveBatch(orderDetailList);
        //封装orderSubmitVO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return Result.success(orderSubmitVO);
    }

    /**
     * 再来一单
     *
     * @param id
     * @return
     */
    @Override
    public Result reSubmitorder(Long id) {
        //根据orderId查询订单详情->将订单详情封装为购物车类->购物车集合添加到数据库/Redis
        LambdaUpdateWrapper<OrderDetail> lqw = new LambdaUpdateWrapper<>();
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetailList = orderDetailService.list(lqw.eq(OrderDetail::getOrderId, id));
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((orderDetail) -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtil.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartService.saveBatch(shoppingCartList);
        redisUtils.set(CAHCE_SHOPINGCART_KEY + userId, JSONUtil.toJsonStr(shoppingCartList), CACHE_SHOPINGCART_TTL, TimeUnit.MINUTES);
        return Result.success();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public Result<OrderPaymentVO> payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);

/*        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }
         */

        JSONObject jsonObject = new JSONObject();
        OrderPaymentVO orderPaymentVO = jsonObject.toJavaObject(OrderPaymentVO.class);
        orderPaymentVO.setPackageStr(jsonObject.getString("package"));
        paySuccess(ordersPaymentDTO.getOrderNumber());
        //支付成功->清空购物车清单
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(lqw);
        //清除购物车缓存
        String key = CAHCE_SHOPINGCART_KEY + userId;
        redisUtils.cleanCache(key);

        return Result.success(orderPaymentVO);
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        Orders ordersDB = getOne(lqw.eq(Orders::getNumber, outTradeNo));
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        updateById(orders);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public Result<OrderVO> queryOrderDetail(Long id) {
        Orders order = getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtil.copyProperties(order, orderVO);
        LambdaQueryWrapper<OrderDetail> lqw = new LambdaQueryWrapper<>();
        List<OrderDetail> orderDetailList = orderDetailService.list(lqw.eq(OrderDetail::getOrderId, id));
        orderVO.setOrderDetailList(orderDetailList);
        return Result.success(orderVO);
    }

    /**
     * 历史订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> ordersPage(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<Orders> pageInfo = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        Long userId = BaseContext.getCurrentId();
        Integer status = ordersPageQueryDTO.getStatus();
        lqw.eq(Orders::getUserId, userId).eq(status != null, Orders::getStatus, status);
        page(pageInfo, lqw);
        List<Orders> records = pageInfo.getRecords();
        List<OrderVO> list = records.stream().map((record) -> {
            OrderVO orderVO = new OrderVO();
            BeanUtil.copyProperties(record, orderVO);
            LambdaQueryWrapper<OrderDetail> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(OrderDetail::getOrderId, orderVO.getId());
            List<OrderDetail> orderDetailList = orderDetailService.list(lqw2);
            orderVO.setOrderDetailList(orderDetailList);
            return orderVO;
        }).collect(Collectors.toList());
        return Result.success(new PageResult(pageInfo.getTotal(), list));
    }

    /**
     * 取消订单
     *
     * @param id
     * @return
     */
    @Override
    public Result cancelOrder(Long id) {
        LambdaUpdateWrapper<Orders> luw = new LambdaUpdateWrapper<>();
        luw.eq(Orders::getId, id)
                .set(Orders::getStatus, Orders.CANCELLED)
                .set(Orders::getCancelTime, LocalDateTime.now())
                .set(Orders::getCancelReason,Orders.CANCEL_REASON);
        update(luw);
        return Result.success();
    }


}
