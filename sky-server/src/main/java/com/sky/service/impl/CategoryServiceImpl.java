package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 分类业务层
 */
@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 启用、禁用分类
     *
     * @param status
     * @param id
     * @return
     */
    public Result<String> startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        updateById(category);
        return Result.success();
    }


    /**
     * 新增分类
     *
     * @param categoryDTO
     */
    @Override
    public Result<String> saveCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtil.copyProperties(categoryDTO, category);
        //分类状态默认为禁用状态0
        category.setStatus(StatusConstant.DISABLE);

        //设置创建时间、修改时间、创建人、修改人
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        save(category);
        return Result.success();
    }

    /**
     * 分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        Page<Category> pageInfo = new Page<>(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(categoryPageQueryDTO.getName()), Category::getName, categoryPageQueryDTO.getName());
        lqw.eq(categoryPageQueryDTO.getType() != null, Category::getType, categoryPageQueryDTO.getType());
        lqw.orderByAsc(Category::getSort);
        Page<Category> page = page(pageInfo, lqw);
        return Result.success(new PageResult(page.getTotal(), page.getRecords()));
    }


    /**
     * 根据id删除分类
     *
     * @param id
     */
    @Override
    public Result<String> deleteCategory(Long id) {
        //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        long count = dishService.count(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        if (count > 0) {
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = setmealService.count(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, id));
        if (count > 0) {
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        //删除分类数据
        removeById(id);
        return Result.success();
    }

    /**
     * 修改分类
     *
     * @param categoryDTO
     */
    @Override
    public Result<String> updateCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtil.copyProperties(categoryDTO, category);
        //设置修改时间、修改人
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        updateById(category);
        return Result.success();
    }
}
