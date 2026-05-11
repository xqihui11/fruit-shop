package com.fruitshop.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.Order;
import com.fruitshop.entity.OrderItem;
import com.fruitshop.entity.Payment;
import com.fruitshop.entity.Product;
import com.fruitshop.entity.ProductSpec;
import com.fruitshop.mapper.OrderItemMapper;
import com.fruitshop.mapper.OrderMapper;
import com.fruitshop.mapper.PaymentMapper;
import com.fruitshop.mapper.ProductMapper;
import com.fruitshop.mapper.ProductSpecMapper;
import com.fruitshop.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {
    @Autowired
    private PaymentMapper paymentMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductSpecMapper productSpecMapper;
    @Override
    @Transactional
    public Map<String, Object> createPayment(Long orderId, String paymentMethod) {
        // 创建支付单：若存在同订单“待支付”记录则复用，保证前端重复点击不产生脏数据。
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("订单状态不正确，无法支付");
        }

        // 检查是否已有“待支付”的支付记录（本地模拟支付同样复用这条记录）
        QueryWrapper<Payment> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        wrapper.eq("status", 0);
        Payment existingPayment = paymentMapper.selectOne(wrapper);
        if (existingPayment != null) {
            // 复用已有支付记录，直接返回支付基础信息（无需真实支付链接）
            Map<String, Object> result = new HashMap<>();
            result.put("paymentNo", existingPayment.getPaymentNo());
            result.put("amount", existingPayment.getAmount());
            result.put("paymentMethod", existingPayment.getPaymentMethod());
            result.put("orderNo", order.getOrderNo());
            return result;
        }

        // 创建支付记录
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setOrderNo(order.getOrderNo());
        payment.setPaymentNo(generatePaymentNo());
        // 这里不再真正区分支付宝/微信等渠道，统一视为“模拟支付”
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "mock");
        payment.setAmount(order.getPayAmount());
        payment.setStatus(0); // 待支付
        paymentMapper.insert(payment);

        // 更新订单支付方式
        order.setPaymentMethod(paymentMethod);
        orderMapper.updateById(order);

        // 返回支付信息（含支付宝沙箱二维码链接）
        Map<String, Object> result = new HashMap<>();
        result.put("paymentNo", payment.getPaymentNo());
        result.put("amount", payment.getAmount());
        result.put("paymentMethod", payment.getPaymentMethod());
        result.put("orderNo", order.getOrderNo());

        return result;
    }

    @Override
    @Transactional
    public void handlePaymentCallback(String paymentNo, boolean success, String failureReason) {
        // 统一支付回调入口（真实支付/模拟支付都走这里），
        // 通过状态判断避免重复扣库存（幂等处理）。
        QueryWrapper<Payment> wrapper = new QueryWrapper<>();
        wrapper.eq("payment_no", paymentNo);
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }

        System.out.println("收到支付回调: paymentNo=" + paymentNo + ", success=" + success);

        if (success) {
            // 检查是否已经支付成功过，避免重复处理
            if (payment.getStatus() == 1) {
                System.out.println("支付记录已处理过，跳过: " + paymentNo);
                return; // 已经处理过，直接返回
            }
            
            System.out.println("开始处理支付成功回调: paymentNo=" + paymentNo);
            
            payment.setStatus(1); // 支付成功
            payment.setPayTime(LocalDateTime.now());
            paymentMapper.updateById(payment);

            // 更新订单状态
            Order order = orderMapper.selectById(payment.getOrderId());
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            
            // 检查订单是否已经支付过，避免重复更新库存
            if (order.getPaymentStatus() == 1) {
                System.out.println("订单已支付过，跳过库存更新: orderId=" + order.getId());
                return; // 订单已经支付过，避免重复更新库存
            }
            
            System.out.println("更新订单状态: orderId=" + order.getId() + ", orderNo=" + order.getOrderNo());
            
            order.setPaymentStatus(1);
            order.setPaymentTime(LocalDateTime.now());
            order.setOrderStatus(1); // 待发货
            orderMapper.updateById(order);

            // 更新商品库存和销量
            System.out.println("开始更新商品库存和销量: orderId=" + order.getId());
            try {
                updateProductStockAndSales(order.getId());
                System.out.println("商品库存和销量更新成功: orderId=" + order.getId());
            } catch (Exception e) {
                // 如果更新库存失败，记录错误并抛出异常，回滚事务
                System.err.println("更新商品库存失败: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("更新商品库存失败: " + e.getMessage(), e);
            }
        } else {
            // 支付失败分支：无论当前状态如何，都标记为失败，并将订单状态调整为待支付，便于沙盒演示
            payment.setStatus(2); // 支付失败
            payment.setFailureReason(failureReason);
            paymentMapper.updateById(payment);

            // 更新订单支付状态和订单状态
            Order order = orderMapper.selectById(payment.getOrderId());
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            Integer oldOrderStatus = order.getOrderStatus();
            Integer oldPayStatus = order.getPaymentStatus();

            order.setPaymentStatus(2); // 支付失败
            // 这里为了方便沙盒测试，不论之前是否标记过成功，都将订单状态重置为待支付
            order.setOrderStatus(0);
            orderMapper.updateById(order);

            System.out.println("处理支付失败回调: orderId=" + order.getId()
                    + ", oldOrderStatus=" + oldOrderStatus
                    + ", newOrderStatus=" + order.getOrderStatus()
                    + ", oldPaymentStatus=" + oldPayStatus
                    + ", newPaymentStatus=" + order.getPaymentStatus());
        }
    }

    /**
     * 模拟支付结果，供前端测试环境调用。
     * 通过订单ID找到最近一条支付记录（如不存在则新建一条待支付记录），
     * 然后复用统一的 {@link #handlePaymentCallback(String, boolean, String)} 逻辑，
     * 保证模拟支付与真实支付回调走同一套状态流转。
     */
    @Override
    @Transactional
    public void simulatePayment(Long orderId, boolean success, String failureReason) {
        if (orderId == null) {
            throw new RuntimeException("订单ID不能为空");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 校验订单当前状态，避免重复支付
        if (Boolean.TRUE.equals(success)
                && order.getPaymentStatus() != null
                && order.getPaymentStatus() == 1) {
            // 已经支付成功的订单不允许再次模拟成功
            throw new RuntimeException("该订单已支付，无需重复支付");
        }

        // 查询最新一条支付记录
        QueryWrapper<Payment> paymentWrapper = new QueryWrapper<>();
        paymentWrapper.eq("order_id", orderId);
        paymentWrapper.orderByDesc("create_time");
        paymentWrapper.last("LIMIT 1");
        Payment payment = paymentMapper.selectOne(paymentWrapper);

        // 如果还没有支付记录，则为本次模拟生成一条待支付记录
        if (payment == null) {
            payment = new Payment();
            payment.setOrderId(orderId);
            payment.setOrderNo(order.getOrderNo());
            payment.setPaymentNo(generatePaymentNo());
            // 若订单尚未指定支付方式，默认使用本地模拟支付方式
            String paymentMethod = order.getPaymentMethod() != null ? order.getPaymentMethod() : "mock";
            payment.setPaymentMethod(paymentMethod);
            payment.setAmount(order.getPayAmount());
            payment.setStatus(0); // 待支付
            paymentMapper.insert(payment);

            // 同步更新订单的支付方式信息，方便前端展示
            if (order.getPaymentMethod() == null) {
                order.setPaymentMethod(paymentMethod);
                orderMapper.updateById(order);
            }
        }

        System.out.println("模拟支付触发，orderId=" + orderId
                + ", paymentNo=" + payment.getPaymentNo()
                + ", success=" + success
                + ", failureReason=" + failureReason);

        // 复用统一的回调处理逻辑，保证库存、订单状态等一致
        handlePaymentCallback(payment.getPaymentNo(), success, failureReason);
    }

    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        QueryWrapper<Payment> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        wrapper.orderByDesc("create_time");
        wrapper.last("LIMIT 1");
        return paymentMapper.selectOne(wrapper);
    }

    /**
     * 更新商品库存和销量
     */
    private void updateProductStockAndSales(Long orderId) {
        // 支付成功后在同事务内完成库存与销量更新，异常时整体回滚。
        // 获取订单的所有订单项
        QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("order_id", orderId);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        if (orderItems == null || orderItems.isEmpty()) {
            System.out.println("订单 " + orderId + " 没有订单项");
            return;
        }

        for (OrderItem orderItem : orderItems) {
            // 如果有规格，先更新规格的库存
            if (orderItem.getSpecId() != null && orderItem.getSpecId() > 0) {
                ProductSpec spec = productSpecMapper.selectById(orderItem.getSpecId());
                if (spec != null) {
                    int currentStock = spec.getStock() != null ? spec.getStock() : 0;
                    int newStock = currentStock - orderItem.getQuantity();
                    if (newStock < 0) {
                        throw new RuntimeException("商品规格库存不足: " + spec.getSpecName());
                    }
                    spec.setStock(newStock);
                    productSpecMapper.updateById(spec);
                    System.out.println("更新规格库存: " + spec.getSpecName() + ", 原库存: " + currentStock + ", 新库存: " + newStock);
                }
            }

            // 更新商品的库存和销量
            Product product = productMapper.selectById(orderItem.getProductId());
            if (product != null) {
                // 减少库存
                int currentStock = product.getStock() != null ? product.getStock() : 0;
                int newStock = currentStock - orderItem.getQuantity();
                if (newStock < 0) {
                    throw new RuntimeException("商品库存不足: " + product.getName());
                }
                product.setStock(newStock);

                // 增加销量
                int currentSales = product.getSalesCount() != null ? product.getSalesCount() : 0;
                int newSales = currentSales + orderItem.getQuantity();
                product.setSalesCount(newSales);

                productMapper.updateById(product);
                System.out.println("更新商品: " + product.getName() + ", 原库存: " + currentStock + ", 新库存: " + newStock + ", 原销量: " + currentSales + ", 新销量: " + newSales);
            } else {
                System.out.println("商品不存在: productId=" + orderItem.getProductId());
            }
        }
    }

    private String generatePaymentNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}

