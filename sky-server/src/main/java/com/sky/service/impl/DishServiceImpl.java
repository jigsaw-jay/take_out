package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishFlavorService;
import com.sky.service.DishService;
import com.sky.service.SetmealDishService;
import com.sky.utils.RedisUtils;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.RedisConstants.*;


@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @Override
    public Result saveDishWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtil.copyProperties(dishDTO, dish);
        save(dish);
        //清理Redis缓存
        String key = CACHE_DISH_KEY + dishDTO.getCategoryId();
        redisUtils.cleanCache(key);
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorService.saveBatch(flavors);
            return Result.success();
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> queryDishPage(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> dishVOPage = dishMapper.queryDishPage(dishPageQueryDTO);
        return Result.success(new PageResult(dishVOPage.getTotal(), dishVOPage.getResult()));
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     * @return
     */
    @Override
    public Result<String> deleteDishWithFlavor(List<Long> ids) {
        LambdaQueryWrapper<Dish> lqw1 = new LambdaQueryWrapper<>();
        List<Dish> dishList = list(lqw1.in(Dish::getId, ids));
        for (Dish dish : dishList) {
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否被套餐关联
        LambdaQueryWrapper<SetmealDish> lqw2 = new LambdaQueryWrapper<>();
        lqw2.in(SetmealDish::getDishId, ids);
        List<SetmealDish> list = setmealDishService.list(lqw2);
        if (list != null && list.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除缓存
        for (Dish dish : dishList) {
            String key = CACHE_DISH_KEY + dish.getCategoryId();
            redisUtils.cleanCache(key);
        }
        //删除菜品
        removeByIds(ids);
        //删除相关口味
        LambdaQueryWrapper<DishFlavor> lqw3 = new LambdaQueryWrapper<>();
        lqw3.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(lqw3);
        return Result.success();
    }

    /**
     * 根据Id查询菜品信息及口味
     *
     * @param id
     * @return
     */
    @Override
    public Result<DishVO> queryById(Long id) {
        DishVO dishVO = new DishVO();
        Dish dish = getById(id);
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(lqw);
        BeanUtil.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return Result.success(dishVO);
    }

    /**
     * 停售起售
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public Result setStatus(Integer status, Long id) {
        //判断当前菜品是否被套餐关联
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getDishId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(lqw);
        if (setmealDishes != null && setmealDishes.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL_2);
        }
        LambdaUpdateWrapper<Dish> luw = new LambdaUpdateWrapper<>();
        luw.eq(Dish::getId, id);
        luw.set(Dish::getStatus, status);
        update(luw);
        return Result.success();
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return
     */
    @Override
    public Result updateDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtil.copyProperties(dishDTO, dish);
        updateById(dish);
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dishDTO.getId());
        dishFlavorService.remove(lqw);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            dishFlavorService.saveBatch(flavors);
        }
        //清理Redis缓存
        String key = CACHE_DISH_KEY + dishDTO.getCategoryId();
        redisUtils.cleanCache(key);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品集合
     *
     * @param categoryId
     * @return
     */
    @Override
    public Result<List> listDish(Integer categoryId) {
        String key = CACHE_DISH_KEY + categoryId;
        String dishJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(dishJson)) {
            List<DishVO> dishVoList = JSONUtil.toList(dishJson, DishVO.class);
            return Result.success(dishVoList);
        }
        LambdaUpdateWrapper<Dish> lqw = new LambdaUpdateWrapper<>();
        List<Dish> dishList = list(lqw.eq(Dish::getCategoryId, categoryId));
        List<DishVO> dishVOList = new ArrayList<>();
        for (Dish dish : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtil.copyProperties(dish, dishVO);
            LambdaQueryWrapper<DishFlavor> flavorlqw = new LambdaQueryWrapper<>();
            Long dishId = dish.getId();
            List<DishFlavor> flavors = dishFlavorService.list(flavorlqw.eq(DishFlavor::getDishId, dishId));
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        redisUtils.set(key, JSONUtil.toJsonStr(dishVOList), CACHE_DISH_TTL, TimeUnit.MINUTES);
        return Result.success(dishVOList);
    }
}
