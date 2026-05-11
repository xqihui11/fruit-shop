package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.AfterSale;
import com.fruitshop.service.AfterSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/aftersale")
public class AfterSaleController {
    @Autowired
    private AfterSaleService afterSaleService;

    @PostMapping("/apply")
    public Result<AfterSale> applyAfterSale(@RequestBody Map<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());
            Long orderItemId = params.get("orderItemId") != null ? Long.valueOf(params.get("orderItemId").toString()) : null;
            Integer type = Integer.valueOf(params.get("type").toString());
            String reason = params.get("reason").toString();
            String description = params.get("description") != null ? params.get("description").toString() : null;
            String images = params.get("images") != null ? params.get("images").toString() : null;
            if (images == null || images.trim().isEmpty()) {
                return Result.error("请至少上传一张凭证图片");
            }
            BigDecimal refundAmount = params.get("refundAmount") != null ? new BigDecimal(params.get("refundAmount").toString()) : null;
            
            AfterSale afterSale = afterSaleService.applyAfterSale(orderId, orderItemId, type, reason, description, images, refundAmount);
            return Result.success("售后申请提交成功", afterSale);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/list")
    public Result<List<AfterSale>> getUserAfterSales() {
        try {
            List<AfterSale> list = afterSaleService.getUserAfterSales();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public Result<AfterSale> getAfterSaleDetail(@PathVariable Long id) {
        try {
            AfterSale afterSale = afterSaleService.getAfterSaleDetail(id);
            return Result.success(afterSale);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户确认已退货（仅针对退货退款类型），同时填写退货物流信息
     */
    @PostMapping("/confirm-return/{id}")
    public Result<Void> confirmReturn(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String company = params.get("logisticsCompany");
            String no = params.get("logisticsNo");
            afterSaleService.confirmReturn(id, company, no);
            return Result.success("确认退货成功，等待退款完成", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户对已驳回的售后发起申诉，请求管理员介入
     */
    @PostMapping("/appeal/{id}")
    public Result<Void> appeal(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String reason = params.get("reason") != null ? params.get("reason").toString() : null;
            String images = params.get("images") != null ? params.get("images").toString() : null;
            afterSaleService.appealAfterSale(id, reason, images);
            return Result.success("申诉已提交，请等待管理员处理", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

