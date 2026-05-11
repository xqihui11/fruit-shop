package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.CartItem;
import com.fruitshop.mapper.CartItemMapper;
import com.fruitshop.service.CartService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {
    @Override
    public void addToCart(Long productId, Long specId, Integer quantity) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        QueryWrapper<CartItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("product_id", productId);
        if (specId != null) {
            wrapper.eq("spec_id", specId);
        } else {
            wrapper.isNull("spec_id");
        }

        CartItem cartItem = getOne(wrapper);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            updateById(cartItem);
        } else {
            cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setSpecId(specId);
            cartItem.setQuantity(quantity);
            cartItem.setIsSelected(1);
            save(cartItem);
        }
    }

    @Override
    public List<CartItem> getCartList() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        QueryWrapper<CartItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return list(wrapper);
    }

    @Override
    public void updateQuantity(Long id, Integer quantity) {
        CartItem cartItem = getById(id);
        if (cartItem != null) {
            cartItem.setQuantity(quantity);
            updateById(cartItem);
        }
    }

    @Override
    public void deleteCartItem(Long id) {
        removeById(id);
    }

    @Override
    public void clearCart() {
        Long userId = UserContext.getUserId();
        QueryWrapper<CartItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        remove(wrapper);
    }

    @Override
    public void updateSelected(List<Long> ids, Integer selected, boolean clearOthers) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        int sel = (selected != null && selected != 0) ? 1 : 0;

        // 先清空当前用户所有选中状态
        if (clearOthers) {
            UpdateWrapper<CartItem> clear = new UpdateWrapper<>();
            clear.eq("user_id", userId).set("is_selected", 0);
            this.update(clear);
        }

        if (ids == null || ids.isEmpty()) {
            // 只清空不设置（或没有指定）
            return;
        }

        UpdateWrapper<CartItem> update = new UpdateWrapper<>();
        update.eq("user_id", userId).in("id", ids).set("is_selected", sel);
        this.update(update);
    }
}

