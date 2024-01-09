package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    Result addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     * @return
     */
    Result<List<ShoppingCart>> queryShoppingCart();

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     * @return
     */
    Result subShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 清空购物车
     * @return
     */
    Result cleanShoppingCart();
}
