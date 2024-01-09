package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 保存套餐
     * @param setmealDTO
     * @return
     */
    Result saveSetmeal(SetmealDTO setmealDTO);

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    Result<PageResult> queryPage(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据Id查询套餐及菜品信息
     * @param id
     * @return
     */
    Result<SetmealVO> queryById(Integer id);

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    Result updateSetmeal(SetmealDTO setmealDTO);

    /**
     * 套餐启售、停售
     * @param status
     * @param id
     * @return
     */
    Result setStatus(Integer status, Long id);

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    Result removeSetmeal(List<Long> ids);

    /**
     * 根据分类id查询套餐
     * @param categoryId
     * @return
     */
    Result setmealList(Integer categoryId);
}
