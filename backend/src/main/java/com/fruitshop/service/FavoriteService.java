package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.FavoriteProduct;
import com.fruitshop.entity.FavoriteMerchant;
import java.util.List;

public interface FavoriteService {
    // 商品收藏
    void addFavoriteProduct(Long productId);
    void removeFavoriteProduct(Long productId);
    boolean isFavoriteProduct(Long productId);
    List<FavoriteProduct> getFavoriteProducts();
    
    // 店铺收藏
    void addFavoriteMerchant(Long merchantId);
    void removeFavoriteMerchant(Long merchantId);
    boolean isFavoriteMerchant(Long merchantId);
    List<FavoriteMerchant> getFavoriteMerchants();
}

