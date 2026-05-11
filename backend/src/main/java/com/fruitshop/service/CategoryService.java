package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Category;
import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> listAll();
    Category addCategory(Category category);
    Category updateCategory(Category category);
    void deleteCategory(Long id);
}

