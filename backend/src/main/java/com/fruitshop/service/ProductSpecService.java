package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.ProductSpec;
import java.util.List;

public interface ProductSpecService extends IService<ProductSpec> {
    List<ProductSpec> getSpecsByProductId(Long productId);
    void saveSpecs(Long productId, List<ProductSpec> specs);
    void deleteSpecsByProductId(Long productId);
}

