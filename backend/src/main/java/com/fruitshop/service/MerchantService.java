package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Merchant;

import java.util.List;
import java.util.Map;

public interface MerchantService extends IService<Merchant> {
    Merchant register(Merchant merchant);
    String login(String username, String password);
    Merchant getCurrentMerchant();
    Merchant updateMerchant(Merchant merchant);
    List<Merchant> getPendingMerchants();
    void auditMerchant(Long id, boolean approve, Long adminId);
    Map<String, Object> getStatistics();
    List<Merchant> getActiveMerchants(); // 获取所有已通过审核的店铺
    Merchant getMerchantDetail(Long id); // 获取店铺详情
}

