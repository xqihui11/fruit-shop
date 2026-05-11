package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.Payment;
import com.fruitshop.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public Result<Map<String, Object>> createPayment(@RequestBody Map<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());
            String paymentMethod = params.get("paymentMethod") != null ? params.get("paymentMethod").toString() : "alipay";
            Map<String, Object> result = paymentService.createPayment(orderId, paymentMethod);
            return Result.success("支付创建成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/callback")
    public Result<Void> handleCallback(@RequestBody Map<String, Object> params) {
        try {
            String paymentNo = params.get("paymentNo").toString();
            boolean success = Boolean.parseBoolean(params.get("success").toString());
            String failureReason = params.get("failureReason") != null ? params.get("failureReason").toString() : null;
            paymentService.handlePaymentCallback(paymentNo, success, failureReason);
            return Result.success("处理成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/status/{orderId}")
    public Result<Payment> getPaymentStatus(@PathVariable Long orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            return Result.success(payment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 模拟支付宝支付（用于演示/沙盒）
    @PostMapping("/simulate")
    public Result<Void> simulatePayment(@RequestBody Map<String, Object> params) {
        try {
            String paymentNo = params.get("paymentNo").toString();
            // 是否模拟支付成功，默认成功
            boolean success = true;
            if (params.get("success") != null) {
                success = Boolean.parseBoolean(params.get("success").toString());
            }
            String failureReason = null;
            if (params.get("failureReason") != null) {
                failureReason = params.get("failureReason").toString();
            }

            // 调用统一的支付回调处理逻辑
            paymentService.handlePaymentCallback(paymentNo, success, failureReason);
            return Result.success(success ? "支付成功" : "支付失败", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 基于订单ID的模拟支付接口，方便前端在订单详情/支付页直接调用。
     *
     * 约定请求体示例：
     * {
     *   "orderId": 123,
     *   "payResult": "success" | "fail",   // 可选，默认 success
     *   "failReason": "余额不足"           // payResult=fail 时可选
     * }
     */
    @PostMapping("/simulateByOrder")
    public Result<Void> simulateByOrder(@RequestBody Map<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());

            // 兼容前端两种写法：payResult 或 success
            boolean success = true;
            if (params.get("payResult") != null) {
                String payResult = params.get("payResult").toString();
                success = "success".equalsIgnoreCase(payResult);
            } else if (params.get("success") != null) {
                success = Boolean.parseBoolean(params.get("success").toString());
            }

            String failureReason = null;
            if (!success) {
                // 支持多种失败场景：余额不足 / 支付超时 / 用户取消 / 系统异常 等
                if (params.get("failReason") != null) {
                    failureReason = params.get("failReason").toString();
                } else if (params.get("failureReason") != null) {
                    failureReason = params.get("failureReason").toString();
                }
            }

            paymentService.simulatePayment(orderId, success, failureReason);
            return Result.success(success ? "模拟支付成功" : "模拟支付失败", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

