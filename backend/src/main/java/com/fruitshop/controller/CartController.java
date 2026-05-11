package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.CartItem;
import com.fruitshop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public Result<Void> addToCart(@RequestBody Map<String, Object> params) {
        try {
            Long productId = Long.valueOf(params.get("productId").toString());
            Long specId = params.get("specId") != null ? Long.valueOf(params.get("specId").toString()) : null;
            Integer quantity = Integer.valueOf(params.get("quantity").toString());
            cartService.addToCart(productId, specId, quantity);
            return Result.success("添加成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<CartItem>> getCartList() {
        try {
            List<CartItem> cartItems = cartService.getCartList();
            return Result.success(cartItems);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> updateQuantity(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer quantity = Integer.valueOf(params.get("quantity").toString());
            cartService.updateQuantity(id, quantity);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCartItem(@PathVariable Long id) {
        try {
            cartService.deleteCartItem(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新购物车项选中状态（用于结算）
     * params:
     * - id: Long (可选，单个更新)
     * - ids: Long[]/List<Long> (可选，批量更新)
     * - selected: 0/1
     * - clearOthers: true/false (可选；为true时先将当前用户所有项置0，再将传入id(s)置selected)
     */
    @PutMapping("/select")
    public Result<Void> updateSelected(@RequestBody Map<String, Object> params) {
        try {
            Integer selected = params.get("selected") != null
                    ? Integer.valueOf(params.get("selected").toString())
                    : 1;
            boolean clearOthers = params.get("clearOthers") != null
                    && Boolean.parseBoolean(params.get("clearOthers").toString());

            List<Long> ids = null;
            if (params.get("id") != null) {
                ids = List.of(Long.valueOf(params.get("id").toString()));
            } else if (params.get("ids") != null) {
                Object raw = params.get("ids");
                if (raw instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) raw;
                    ids = list.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
                }
            }

            cartService.updateSelected(ids, selected, clearOthers);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

