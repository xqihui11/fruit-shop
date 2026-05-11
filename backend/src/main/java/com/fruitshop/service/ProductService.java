package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Product;
import java.util.List;

public interface ProductService extends IService<Product> {
    List<Product> listProducts(Long categoryId, Long merchantId, String keyword, String sortBy, String priceRange, Integer freshnessLevel, Boolean specialOnly);
    Product getProductDetail(Long id);
    
    // 商家端方法
    Product addProduct(Product product);
    Product updateProduct(Product product);
    void updateProductStatus(Long id, Integer status);
    List<Product> getMerchantProducts(Integer status);
    
    // 管理员端方法
    List<Product> getAdminProductList(Integer status);
    List<Product> getPendingProducts();
    void auditProduct(Long id, boolean approve, String remark, Long adminId);
}

