package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Order;
import com.fruitshop.entity.OrderItem;
import java.util.List;

public interface OrderService extends IService<Order> {
    Order createOrder(Long addressId, String remark, Long couponId);
    List<Order> getOrderList(Integer status);
    Order getOrderDetail(Long id);
    void cancelOrder(Long id, String reason);
    void confirmReceive(Long id);
    List<OrderItem> getOrderItems(Long orderId);
    
    // 商家端方法
    List<Order> getMerchantOrders(Integer status);
    Order getMerchantOrderDetail(Long id);
    void shipOrder(Long id, String logisticsCompany, String logisticsNo);
    
    // 管理员端方法
    List<Order> getAdminOrderList(Integer status);
}

