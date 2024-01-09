package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    public Result addShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("message:{}", shoppingCartDTO);
        return shoppingCartService.addShoppingCart(shoppingCartDTO);
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> queryShoppingCart() {
        return shoppingCartService.queryShoppingCart();
    }

    /**
     * 删除购物车中一个商品
     *
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/sub")
    public Result subShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        return shoppingCartService.subShoppingCart(shoppingCartDTO);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result cleanShoppingCart() {
        return shoppingCartService.cleanShoppingCart();
    }
}
