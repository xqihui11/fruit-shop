package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.CartItem;
import java.util.List;

public interface CartService extends IService<CartItem> {
    void addToCart(Long productId, Long specId, Integer quantity);
    List<CartItem> getCartList();
    void updateQuantity(Long id, Integer quantity);
    void deleteCartItem(Long id);
    void clearCart();
    void updateSelected(List<Long> ids, Integer selected, boolean clearOthers);
}

