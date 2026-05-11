package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.Order;
import com.fruitshop.entity.OrderItem;
import com.fruitshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody Map<String, Object> params) {
        try {
            Long addressId = Long.valueOf(params.get("addressId").toString());
            String remark = params.get("remark") != null ? params.get("remark").toString() : null;
            Long couponId = params.get("couponId") != null ? Long.valueOf(params.get("couponId").toString()) : null;
            Order order = orderService.createOrder(addressId, remark, couponId);
            return Result.success("订单创建成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Order>> getOrderList(@RequestParam(required = false) Integer status) {
        try {
            List<Order> orders = orderService.getOrderList(status);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderDetail(id);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String reason = params.get("reason");
            orderService.cancelOrder(id, reason);
            return Result.success("订单已取消", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/confirm/{id}")
    public Result<Void> confirmReceive(@PathVariable Long id) {
        try {
            orderService.confirmReceive(id);
            return Result.success("确认收货成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/items/{orderId}")
    public Result<List<OrderItem>> getOrderItems(@PathVariable Long orderId) {
        try {
            List<OrderItem> items = orderService.getOrderItems(orderId);
            return Result.success(items);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

