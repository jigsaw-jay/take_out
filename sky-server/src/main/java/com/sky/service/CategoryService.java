package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;

import java.util.List;

public interface CategoryService extends IService<Category> {



    /**
     * 新增分类
     *
     * @param categoryDTO
     */
    Result<String> saveCategory(CategoryDTO categoryDTO);

    /**
     * 分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    Result<PageResult> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据id删除分类
     *
     * @param id
     */
    Result<String> deleteCategory(Long id);

    /**
     * 修改分类
     *
     * @param categoryDTO
     */
    Result<String> updateCategory(CategoryDTO categoryDTO);

    /**
     * 启用、禁用分类
     *
     * @param status
     * @param id
     */
    Result<String> startOrStop(Integer status, Long id);
}
