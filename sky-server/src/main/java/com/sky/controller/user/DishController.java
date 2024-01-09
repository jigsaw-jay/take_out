package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @GetMapping("/list")
    public Result<List> listDish(Integer categoryId) {
        return dishService.listDish(categoryId);
    }
}
