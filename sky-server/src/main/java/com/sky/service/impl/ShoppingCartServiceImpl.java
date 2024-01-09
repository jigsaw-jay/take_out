package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.RedisConstants.CACHE_SHOPINGCART_TTL;
import static com.sky.constant.RedisConstants.CAHCE_SHOPINGCART_KEY;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     * @return
     */
    @Override
    public Result addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtil.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        String key = CAHCE_SHOPINGCART_KEY + userId;
        shoppingCart.setUserId(userId);
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        lqw.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        lqw.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        lqw.eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        ShoppingCart cart = getOne(lqw);
        //已经存在该数据
        if (cart != null) {
            cart.setNumber(cart.getNumber() + 1);
            updateById(cart);
            redisUtils.cleanCache(key);
        } else {
            //不存在->判断加入的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if (dishId == null) {
                //是套餐
                Setmeal setmeal = setmealService.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            } else {
                //是菜品
                Dish dish = dishService.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            save(shoppingCart);
            redisUtils.cleanCache(key);
        }
        return Result.success();
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @Override
    public Result<List<ShoppingCart>> queryShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        String key = CAHCE_SHOPINGCART_KEY + userId;
        String shoppingCartJson = stringRedisTemplate.opsForValue().get(key);
        //缓存中存在->直接返回
        if (StrUtil.isNotBlank(shoppingCartJson)) {
            List<ShoppingCart> shoppingCartList = JSONUtil.toList(shoppingCartJson, ShoppingCart.class);
            return Result.success(shoppingCartList);
        }
        //缓存中不存在->查询数据库->保存缓存
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        List<ShoppingCart> shoppingCartList = list(lqw.eq(ShoppingCart::getUserId, userId));
        redisUtils.set(key, JSONUtil.toJsonStr(shoppingCartList), CACHE_SHOPINGCART_TTL, TimeUnit.MINUTES);
        return Result.success(shoppingCartList);
    }

    /**
     * 删除购物车中一个商品
     *
     * @param shoppingCartDTO
     * @return
     */
    @Override
    public Result subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //log.info("info:{}", shoppingCartDTO);
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtil.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        String key = CAHCE_SHOPINGCART_KEY + userId;
        shoppingCart.setUserId(userId);
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        lqw.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        lqw.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        lqw.eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        ShoppingCart cart = getOne(lqw);
        if (cart.getNumber() == 1) {
            removeById(cart.getId());
        } else {
            cart.setNumber(cart.getNumber() - 1);
            updateById(cart);
        }
        redisUtils.cleanCache(key);
        return Result.success();
    }

    /**
     * 清空购物车
     * @return
     */
    @Override
    public Result cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        String key=CAHCE_SHOPINGCART_KEY+userId;
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        remove(lqw.eq(ShoppingCart::getUserId, userId));
        redisUtils.cleanCache(key);
        return Result.success();
    }
}
