package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.Category;
import com.fruitshop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> listAll() {
        try {
            List<Category> categories = categoryService.listAll();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

