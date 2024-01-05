package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;

public interface SetmealService extends IService<Setmeal> {
    Result saveSetmeal(SetmealDTO setmealDTO);

    Result<PageResult> queryPage(SetmealPageQueryDTO setmealPageQueryDTO);
}
