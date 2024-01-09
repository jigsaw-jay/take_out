package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.DishFlavor;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Tag(name = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增菜品")
    public Result saveDishWithFlavor(@RequestBody DishDTO dishDTO) {
        return dishService.saveDishWithFlavor(dishDTO);
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "菜品分页查询")
    public Result<PageResult> queryDishPage(@ParameterObject DishPageQueryDTO dishPageQueryDTO) {
        return dishService.queryDishPage(dishPageQueryDTO);
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping()
    @Operation(summary = "批量删除菜品")
    public Result<String> deleteDishWithFlavor(@RequestParam List<Long> ids) {
        return dishService.deleteDishWithFlavor(ids);
    }

    /**
     * 根据Id查询菜品信息及口味
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据Id查询菜品信息及口味")
    public Result<DishVO> queryById(@PathVariable Long id) {
        return dishService.queryById(id);
    }

    /**
     * 停售起售
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "停售起售")
    public Result setStatus(@PathVariable Integer status, Long id) {
        return dishService.setStatus(status, id);
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return
     */
    @PutMapping()
    @Operation(summary = "修改菜品信息")
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        return dishService.updateDish(dishDTO);
    }

    /**
     * 根据分类id查询菜品集合
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "根据分类id查询菜品集合")
    public Result<List> listDish(Integer categoryId) {
        return dishService.listDish(categoryId);
    }
}
