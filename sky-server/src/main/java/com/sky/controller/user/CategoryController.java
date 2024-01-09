package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Tag(name = "C端分类相关接口")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping("/list")
    public Result<List> categoryList(Integer type) {
        log.info("type:{}", type);
        return categoryService.categoryList(type);
    }
}
