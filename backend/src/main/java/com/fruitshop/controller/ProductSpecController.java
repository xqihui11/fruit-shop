package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.ProductSpec;
import com.fruitshop.service.ProductSpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/spec")
public class ProductSpecController {
    @Autowired
    private ProductSpecService productSpecService;

    @GetMapping("/list/{productId}")
    public Result<List<ProductSpec>> getSpecsByProductId(@PathVariable Long productId) {
        try {
            List<ProductSpec> specs = productSpecService.getSpecsByProductId(productId);
            return Result.success(specs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/save")
    public Result<Void> saveSpecs(@RequestBody Map<String, Object> params) {
        try {
            Long productId = Long.valueOf(params.get("productId").toString());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> specsData = (List<Map<String, Object>>) params.get("specs");
            
            List<ProductSpec> specs = specsData.stream().map(data -> {
                ProductSpec spec = new ProductSpec();
                if (data.get("id") != null) {
                    spec.setId(Long.valueOf(data.get("id").toString()));
                }
                spec.setSpecName(data.get("specName").toString());
                spec.setSpecValue(data.get("specValue") != null ? data.get("specValue").toString() : null);
                spec.setPrice(new java.math.BigDecimal(data.get("price").toString()));
                if (data.get("originalPrice") != null) {
                    spec.setOriginalPrice(new java.math.BigDecimal(data.get("originalPrice").toString()));
                }
                spec.setStock(Integer.valueOf(data.get("stock").toString()));
                if (data.get("sortOrder") != null) {
                    spec.setSortOrder(Integer.valueOf(data.get("sortOrder").toString()));
                }
                return spec;
            }).collect(java.util.stream.Collectors.toList());
            
            productSpecService.saveSpecs(productId, specs);
            return Result.success("保存成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

