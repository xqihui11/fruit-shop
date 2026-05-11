package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.FavoriteProduct;
import com.fruitshop.entity.FavoriteMerchant;
import com.fruitshop.entity.Product;
import com.fruitshop.entity.Merchant;
import com.fruitshop.mapper.FavoriteProductMapper;
import com.fruitshop.mapper.FavoriteMerchantMapper;
import com.fruitshop.mapper.ProductMapper;
import com.fruitshop.mapper.MerchantMapper;
import com.fruitshop.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    @Autowired
    private FavoriteProductMapper favoriteProductMapper;
    @Autowired
    private FavoriteMerchantMapper favoriteMerchantMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public void addFavoriteProduct(Long productId) {
        Long userId = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            throw new RuntimeException("请先登录");
        }
        
        QueryWrapper<FavoriteProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("product_id", productId);
        FavoriteProduct existing = favoriteProductMapper.selectOne(wrapper);
        if (existing != null) {
            throw new RuntimeException("已收藏该商品");
        }
        
        FavoriteProduct favorite = new FavoriteProduct();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteProductMapper.insert(favorite);
        
        // 验证插入的数据确实属于当前用户
        FavoriteProduct inserted = favoriteProductMapper.selectById(favorite.getId());
        if (inserted == null || !inserted.getUserId().equals(userId)) {
            throw new RuntimeException("收藏失败：数据验证错误");
        }
    }

    @Override
    public void removeFavoriteProduct(Long productId) {
        Long userId = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            throw new RuntimeException("请先登录");
        }
        
        QueryWrapper<FavoriteProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("product_id", productId);
        int deleted = favoriteProductMapper.delete(wrapper);
        
        // 验证删除的是当前用户的收藏
        if (deleted > 0) {
            // 再次查询确认已删除
            QueryWrapper<FavoriteProduct> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("user_id", userId);
            checkWrapper.eq("product_id", productId);
            if (favoriteProductMapper.selectCount(checkWrapper) > 0) {
                throw new RuntimeException("取消收藏失败：数据验证错误");
            }
        }
    }

    @Override
    public boolean isFavoriteProduct(Long productId) {
        Long userId = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            return false;
        }
        
        QueryWrapper<FavoriteProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("product_id", productId);
        FavoriteProduct favorite = favoriteProductMapper.selectOne(wrapper);
        
        // 二次验证：确保查询到的收藏属于当前用户
        return favorite != null && favorite.getUserId() != null && favorite.getUserId().equals(userId);
    }

    @Override
    public List<FavoriteProduct> getFavoriteProducts() {
        Long userId = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            throw new RuntimeException("请先登录");
        }
        
        // 严格验证：确保userId不为空且有效，防止查询到其他用户的数据
        QueryWrapper<FavoriteProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        List<FavoriteProduct> favorites = favoriteProductMapper.selectList(wrapper);
        
        // 二次验证：确保所有收藏都属于当前用户，严格过滤
        if (favorites != null) {
            favorites = favorites.stream()
                    .filter(f -> {
                        if (f == null) return false;
                        Long fUserId = f.getUserId();
                        return fUserId != null && fUserId.equals(userId) && fUserId > 0;
                    })
                    .collect(java.util.stream.Collectors.toList());
        } else {
            favorites = new java.util.ArrayList<>();
        }
        
        // 填充商品信息
        for (FavoriteProduct favorite : favorites) {
            if (favorite != null && favorite.getProductId() != null) {
                Product product = productMapper.selectById(favorite.getProductId());
                favorite.setProduct(product);
            }
        }
        
        return favorites;
    }

    @Override
    public void addFavoriteMerchant(Long merchantId) {
        Long userId = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            throw new RuntimeException("请先登录");
        }
        
        QueryWrapper<FavoriteMerchant> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("merchant_id", merchantId);
        FavoriteMerchant existing = favoriteMerchantMapper.selectOne(wrapper);
        if (existing != null) {
            throw new RuntimeException("已收藏该店铺");
        }
        
        FavoriteMerchant favorite = new FavoriteMerchant();
        favorite.setUserId(userId);
        favorite.setMerchantId(merchantId);
        favoriteMerchantMapper.insert(favorite);
        
        // 验证插入的数据确实属于当前用户
        FavoriteMerchant inserted = favoriteMerchantMapper.selectById(favorite.getId());
        if (inserted == null || !inserted.getUserId().equals(userId)) {
            throw new RuntimeException("收藏失败：数据验证错误");
        }
    }

    @Override
    public void removeFavoriteMerchant(Long merchantId) {
        Long userId = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            throw new RuntimeException("请先登录");
        }
        
        QueryWrapper<FavoriteMerchant> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("merchant_id", merchantId);
        int deleted = favoriteMerchantMapper.delete(wrapper);
        
        // 验证删除的是当前用户的收藏
        if (deleted > 0) {
            // 再次查询确认已删除
            QueryWrapper<FavoriteMerchant> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("user_id", userId);
            checkWrapper.eq("merchant_id", merchantId);
            if (favoriteMerchantMapper.selectCount(checkWrapper) > 0) {
                throw new RuntimeException("取消收藏失败：数据验证错误");
            }
        }
    }

    @Override
    public boolean isFavoriteMerchant(Long merchantId) {
        Long userId = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            return false;
        }
        
        QueryWrapper<FavoriteMerchant> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("merchant_id", merchantId);
        FavoriteMerchant favorite = favoriteMerchantMapper.selectOne(wrapper);
        
        // 二次验证：确保查询到的收藏属于当前用户
        return favorite != null && favorite.getUserId() != null && favorite.getUserId().equals(userId);
    }

    @Override
    public List<FavoriteMerchant> getFavoriteMerchants() {
        Long userId = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            throw new RuntimeException("请先登录");
        }
        
        // 严格验证：确保userId不为空且有效，防止查询到其他用户的数据
        QueryWrapper<FavoriteMerchant> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        List<FavoriteMerchant> favorites = favoriteMerchantMapper.selectList(wrapper);
        
        // 二次验证：确保所有收藏都属于当前用户，严格过滤
        if (favorites != null) {
            favorites = favorites.stream()
                    .filter(f -> {
                        if (f == null) return false;
                        Long fUserId = f.getUserId();
                        return fUserId != null && fUserId.equals(userId) && fUserId > 0;
                    })
                    .collect(java.util.stream.Collectors.toList());
        } else {
            favorites = new java.util.ArrayList<>();
        }
        
        // 填充店铺信息
        for (FavoriteMerchant favorite : favorites) {
            if (favorite != null && favorite.getMerchantId() != null) {
                Merchant merchant = merchantMapper.selectById(favorite.getMerchantId());
                favorite.setMerchant(merchant);
            }
        }
        
        return favorites;
    }
}

