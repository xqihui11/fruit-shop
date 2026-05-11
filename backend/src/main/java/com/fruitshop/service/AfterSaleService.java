package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.AfterSale;

import java.math.BigDecimal;
import java.util.List;

public interface AfterSaleService extends IService<AfterSale> {
    AfterSale applyAfterSale(Long orderId, Long orderItemId, Integer type, String reason, String description, String images, BigDecimal refundAmount);
    List<AfterSale> getUserAfterSales();
    List<AfterSale> getMerchantAfterSales(Integer status);
    void processAfterSale(Long id, boolean agree, String reply);
    AfterSale getAfterSaleDetail(Long id);

    /**
     * 用户确认已退货（仅针对 type=1 退货退款）
     */
    void confirmReturn(Long id, String logisticsCompany, String logisticsNo);

    /**
     * 用户对已驳回的售后发起申诉，请求管理员介入
     */
    void appealAfterSale(Long id, String appealReason, String images);

    /**
     * 管理员查看售后申诉列表
     */
    List<AfterSale> getAdminAppeals(Integer status);

    /**
     * 管理员处理售后申诉
     */
    void adminProcessAppeal(Long id, boolean approve, String remark);
}

