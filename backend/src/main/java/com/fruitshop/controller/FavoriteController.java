package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.FavoriteProduct;
import com.fruitshop.entity.FavoriteMerchant;
import com.fruitshop.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    // 商品收藏
    @PostMapping("/product/add")
    public Result<Void> addFavoriteProduct(@RequestBody Map<String, Object> params) {
        try {
            Long productId = Long.valueOf(params.get("productId").toString());
            favoriteService.addFavoriteProduct(productId);
            return Result.success("收藏成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/product/remove/{productId}")
    public Result<Void> removeFavoriteProduct(@PathVariable Long productId) {
        try {
            favoriteService.removeFavoriteProduct(productId);
            return Result.success("取消收藏成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/product/check/{productId}")
    public Result<Boolean> isFavoriteProduct(@PathVariable Long productId) {
        try {
            boolean isFavorite = favoriteService.isFavoriteProduct(productId);
            return Result.success(isFavorite);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/product/list")
    public Result<List<FavoriteProduct>> getFavoriteProducts() {
        try {
            List<FavoriteProduct> favorites = favoriteService.getFavoriteProducts();
            return Result.success(favorites);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 店铺收藏
    @PostMapping("/merchant/add")
    public Result<Void> addFavoriteMerchant(@RequestBody Map<String, Object> params) {
        try {
            Long merchantId = Long.valueOf(params.get("merchantId").toString());
            favoriteService.addFavoriteMerchant(merchantId);
            return Result.success("收藏成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/merchant/remove/{merchantId}")
    public Result<Void> removeFavoriteMerchant(@PathVariable Long merchantId) {
        try {
            favoriteService.removeFavoriteMerchant(merchantId);
            return Result.success("取消收藏成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/merchant/check/{merchantId}")
    public Result<Boolean> isFavoriteMerchant(@PathVariable Long merchantId) {
        try {
            boolean isFavorite = favoriteService.isFavoriteMerchant(merchantId);
            return Result.success(isFavorite);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/merchant/list")
    public Result<List<FavoriteMerchant>> getFavoriteMerchants() {
        try {
            List<FavoriteMerchant> favorites = favoriteService.getFavoriteMerchants();
            return Result.success(favorites);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

