package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.Product;
import com.fruitshop.mapper.ProductMapper;
import com.fruitshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> listProducts(Long categoryId, Long merchantId, String keyword, String sortBy, String priceRange, Integer freshnessLevel, Boolean specialOnly) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 只查询已上架商品

        // specialOnly:
        // - true：仅特价商品
        // - false：仅非特价商品
        // - null（前端不传）：不做 special_enabled 过滤，避免把列表过滤成空
        if (Boolean.TRUE.equals(specialOnly)) {
            wrapper.eq("special_enabled", 1);
        } else if (specialOnly != null && Boolean.FALSE.equals(specialOnly)) {
            wrapper.ne("special_enabled", 1);
        }
        
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        if (merchantId != null) {
            wrapper.eq("merchant_id", merchantId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("description", keyword));
        }
        
        // 价格区间筛选
        if (priceRange != null && !priceRange.isEmpty()) {
            String[] range = priceRange.split("-");
            if (range.length == 2) {
                try {
                    java.math.BigDecimal minPrice = new java.math.BigDecimal(range[0]);
                    java.math.BigDecimal maxPrice = new java.math.BigDecimal(range[1]);
                    wrapper.ge("price", minPrice);
                    wrapper.le("price", maxPrice);
                } catch (Exception e) {
                    // 忽略无效的价格区间
                    e.printStackTrace();
                }
            }
        }
        
        // 新鲜度筛选
        if (freshnessLevel != null) {
            wrapper.ge("freshness_level", freshnessLevel);
        }
        
        if ("price_asc".equals(sortBy)) {
            wrapper.orderByAsc("price");
        } else if ("price_desc".equals(sortBy)) {
            wrapper.orderByDesc("price");
        } else if ("sales".equals(sortBy)) {
            wrapper.orderByDesc("sales_count");
        } else if ("freshness".equals(sortBy)) {
            wrapper.orderByDesc("freshness_level");
        } else {
            wrapper.orderByDesc("create_time");
        }
        
        return list(wrapper);
    }

    @Override
    public Product getProductDetail(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return product;
    }

    @Override
    public Product addProduct(Product product) {
        Long merchantId = UserContext.getUserId();
        if (merchantId == null) {
            throw new RuntimeException("请先登录");
        }
        product.setMerchantId(merchantId);
        product.setStatus(0); // 待审核
        product.setSalesCount(0);
        normalizeSpecialConfig(product);
        productMapper.insert(product);
        return product;
    }

    @Override
    public Product updateProduct(Product product) {
        Long merchantId = UserContext.getUserId();
        Product existing = productMapper.selectById(product.getId());
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("商品不存在或无权操作");
        }
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setMainImage(product.getMainImage());
        existing.setPrice(product.getPrice());
        existing.setOriginalPrice(product.getOriginalPrice());
        existing.setStock(product.getStock());
        existing.setCategoryId(product.getCategoryId());
        existing.setFreshnessLevel(product.getFreshnessLevel());

        // 特价字段
        existing.setSpecialEnabled(product.getSpecialEnabled());
        existing.setSpecialType(product.getSpecialType());
        existing.setSpecialDiscount(product.getSpecialDiscount());
        existing.setSpecialBuy(product.getSpecialBuy());
        existing.setSpecialFree(product.getSpecialFree());
        existing.setSpecialLabel(product.getSpecialLabel());
        normalizeSpecialConfig(existing);

        productMapper.updateById(existing);
        return existing;
    }

    @Override
    public void updateProductStatus(Long id, Integer status) {
        Long merchantId = UserContext.getUserId();
        Product product = productMapper.selectById(id);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("商品不存在或无权操作");
        }
        // 商家只能上架/下架已审核通过的商品
        if (status == 1 && product.getStatus() == 3) {
            throw new RuntimeException("商品审核未通过，无法上架");
        }
        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Override
    public List<Product> getMerchantProducts(Integer status) {
        Long merchantId = UserContext.getUserId();
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> getAdminProductList(Integer status) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> getPendingProducts() {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.orderByAsc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public void auditProduct(Long id, boolean approve, String remark, Long adminId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus(approve ? 1 : 3);
        product.setAuditTime(LocalDateTime.now());
        product.setAuditAdminId(adminId);
        product.setAuditRemark(remark);
        productMapper.updateById(product);
    }

    private void normalizeSpecialConfig(Product product) {
        if (product == null) return;

        Integer enabled = product.getSpecialEnabled();
        if (enabled == null || enabled != 1) {
            product.setSpecialEnabled(0);
            product.setSpecialType(null);
            product.setSpecialDiscount(null);
            product.setSpecialBuy(null);
            product.setSpecialFree(null);
            product.setSpecialLabel(null);
            return;
        }

        String type = product.getSpecialType();
        // 如果开启了特价但未选择特价方式，则仅作为“进入特价专区”的标记，不做价格和标签处理
        if (type == null || type.trim().isEmpty()) {
            return;
        }

        if ("DISCOUNT".equalsIgnoreCase(type)) {
            BigDecimal discount = product.getSpecialDiscount();
            if (discount == null) {
                throw new RuntimeException("请选择折扣");
            }
            if (discount.compareTo(BigDecimal.ZERO) <= 0 || discount.compareTo(BigDecimal.ONE) > 0) {
                throw new RuntimeException("折扣必须在 0~1 之间（如 0.8 表示 8 折）");
            }
            if (product.getOriginalPrice() == null) {
                throw new RuntimeException("设置折扣时请填写原价");
            }
            BigDecimal newPrice = product.getOriginalPrice()
                    .multiply(discount)
                    .setScale(2, RoundingMode.HALF_UP);
            product.setPrice(newPrice);

            // 默认标签：8折/9.5折等
            if (product.getSpecialLabel() == null || product.getSpecialLabel().trim().isEmpty()) {
                BigDecimal ten = discount.multiply(new BigDecimal("10")).stripTrailingZeros();
                product.setSpecialLabel(ten.toPlainString() + "折");
            }

            // 清理不相关字段
            product.setSpecialBuy(null);
            product.setSpecialFree(null);
        } else if ("BUY_N_GET_M".equalsIgnoreCase(type)) {
            Integer buy = product.getSpecialBuy();
            Integer free = product.getSpecialFree();
            if (buy == null || buy <= 0 || free == null || free <= 0) {
                throw new RuntimeException("买N送M需要填写 N 和 M（且都大于0）");
            }
            if (product.getSpecialLabel() == null || product.getSpecialLabel().trim().isEmpty()) {
                product.setSpecialLabel("买" + buy + "送" + free);
            }

            // 清理不相关字段
            product.setSpecialDiscount(null);
        } else {
            throw new RuntimeException("不支持的特价类型：" + type);
        }
    }
}

