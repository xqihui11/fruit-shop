package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.Merchant;
import com.fruitshop.entity.Product;
import com.fruitshop.entity.ProductSpec;
import com.fruitshop.service.MerchantService;
import com.fruitshop.service.ProductService;
import com.fruitshop.service.ProductSpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ProductSpecService productSpecService;

    @GetMapping("/list")
    public Result<List<Product>> listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) Integer freshnessLevel,
            @RequestParam(required = false) Boolean specialOnly) {
        try {
            List<Product> products = productService.listProducts(categoryId, merchantId, keyword, sortBy, priceRange, freshnessLevel, specialOnly);
            return Result.success(products);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        try {
            Product product = productService.getProductDetail(id);
            Map<String, Object> result = new HashMap<>();
            result.put("product", product);
            
            // 获取店铺信息
            if (product.getMerchantId() != null) {
                Merchant merchant = merchantService.getById(product.getMerchantId());
                if (merchant != null) {
                    merchant.setPassword(null);
                }
                result.put("merchant", merchant);
            }
            
            // 获取商品规格
            List<ProductSpec> specs = productSpecService.getSpecsByProductId(id);
            result.put("specs", specs);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

