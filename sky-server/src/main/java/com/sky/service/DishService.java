package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {
    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    Result saveDishWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Result<PageResult> queryDishPage(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    Result<String> deleteDishWithFlavor(List<Long> ids);

    /**
     * 根据Id查询菜品信息及口味
     * @param id
     * @return
     */
    Result<DishVO> queryById(Long id);

    /**
     * 停售起售
     * @param status
     * @param id
     * @return
     */
    Result setStatus(Integer status, Long id);

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    Result updateDish(DishDTO dishDTO);
}
