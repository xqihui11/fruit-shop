package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Payment;

import java.util.Map;

public interface PaymentService extends IService<Payment> {
    Map<String, Object> createPayment(Long orderId, String paymentMethod);
    void handlePaymentCallback(String paymentNo, boolean success, String failureReason);
    Payment getPaymentByOrderId(Long orderId);

    /**
     * 模拟支付结果（用于前端弹窗中的“测试支付成功/失败/超时”等按钮）
     *
     * @param orderId       订单ID
     * @param success       是否支付成功
     * @param failureReason 失败原因（当 success=false 时可选填写，如：余额不足、支付超时、用户取消、系统异常等）
     */
    void simulatePayment(Long orderId, boolean success, String failureReason);
}

