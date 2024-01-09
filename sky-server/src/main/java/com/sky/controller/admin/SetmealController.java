package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     *
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增套餐")
    public Result saveSetmeal(@RequestBody SetmealDTO setmealDTO) {
        return setmealService.saveSetmeal(setmealDTO);
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "套餐分页查询")
    public Result<PageResult> queryPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        return setmealService.queryPage(setmealPageQueryDTO);
    }

    /**
     * 根据Id查询套餐及菜品信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据Id查询套餐及菜品信息")
    public Result<SetmealVO> queryById(@PathVariable Integer id) {
        return setmealService.queryById(id);
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return
     */
    @PutMapping()
    @Operation(summary = "修改套餐")
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO) {
        return setmealService.updateSetmeal(setmealDTO);
    }

    /**
     * 套餐启售、停售
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "套餐启售、停售")
    public Result setStatus(@PathVariable Integer status, Long id) {
        return setmealService.setStatus(status, id);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @Operation(summary = "批量删除套餐")
    public Result removeSetmeal(@RequestParam List<Long> ids) {
        return setmealService.removeSetmeal(ids);
    }
}
