package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealDishService;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增套餐
     *
     * @param setmealDTO
     * @return
     */
    @Override
    public Result saveSetmeal(SetmealDTO setmealDTO) {
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        Setmeal one = getOne(lqw.eq(Setmeal::getName, setmealDTO.getName()));
        if (one != null) {
            throw new SetmealEnableFailedException("套餐名称已存在!");
        }
        //存储套餐
        Setmeal setmeal = new Setmeal();
        BeanUtil.copyProperties(setmealDTO, setmeal);
        save(setmeal);
        //存储套餐相关菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
        setmealDishService.saveBatch(setmealDishes);
        return Result.success();
    }

    @Override
    public Result<PageResult> queryPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> setmealVOPage = setmealMapper.queryPage(setmealPageQueryDTO);
        return Result.success(new PageResult(setmealVOPage.getTotal(), setmealVOPage.getResult()));
    }
}
