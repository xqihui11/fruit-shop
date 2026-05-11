package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.entity.Category;
import com.fruitshop.mapper.CategoryMapper;
import com.fruitshop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByAsc("sort_order");
        return list(wrapper);
    }

    @Override
    public Category addCategory(Category category) {
        category.setStatus(1);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category updateCategory(Category category) {
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        existing.setName(category.getName());
        existing.setParentId(category.getParentId());
        existing.setSortOrder(category.getSortOrder());
        existing.setStatus(category.getStatus());
        categoryMapper.updateById(existing);
        return existing;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        // 检查是否有子分类
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该分类下有子分类，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}

