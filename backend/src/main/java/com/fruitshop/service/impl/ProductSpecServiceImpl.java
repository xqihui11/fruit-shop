package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.entity.ProductSpec;
import com.fruitshop.mapper.ProductSpecMapper;
import com.fruitshop.service.ProductSpecService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductSpecServiceImpl extends ServiceImpl<ProductSpecMapper, ProductSpec> implements ProductSpecService {
    
    @Override
    public List<ProductSpec> getSpecsByProductId(Long productId) {
        QueryWrapper<ProductSpec> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        wrapper.orderByAsc("sort_order");
        return list(wrapper);
    }

    @Override
    @Transactional
    public void saveSpecs(Long productId, List<ProductSpec> specs) {
        // 先删除旧的规格
        deleteSpecsByProductId(productId);
        
        // 保存新的规格
        if (specs != null && !specs.isEmpty()) {
            for (ProductSpec spec : specs) {
                spec.setProductId(productId);
                save(spec);
            }
        }
    }

    @Override
    @Transactional
    public void deleteSpecsByProductId(Long productId) {
        QueryWrapper<ProductSpec> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        remove(wrapper);
    }
}

