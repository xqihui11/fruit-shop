package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.AfterSale;
import com.fruitshop.entity.Order;
import com.fruitshop.mapper.AfterSaleMapper;
import com.fruitshop.mapper.OrderMapper;
import com.fruitshop.mapper.OrderItemMapper;
import com.fruitshop.service.AfterSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

@Service
public class AfterSaleServiceImpl extends ServiceImpl<AfterSaleMapper, AfterSale> implements AfterSaleService {
    @Autowired
    private AfterSaleMapper afterSaleMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public AfterSale applyAfterSale(Long orderId, Long orderItemId, Integer type, String reason, String description, String images, BigDecimal refundAmount) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }

        // 检查订单状态（待收货、待评价、已完成状态可以申请售后）
        if (order.getOrderStatus() < 2 || order.getOrderStatus() > 4) {
            throw new RuntimeException("当前订单状态不支持售后申请");
        }

        // 检查是否已有未完结的售后申请（避免用户反复申请）
        QueryWrapper<AfterSale> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        wrapper.eq("user_id", userId);
        wrapper.in("status", Arrays.asList(0, 1, 4)); // 待处理 / 待退货 / 申诉中
        if (afterSaleMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("存在未完结的售后申请，无法重复申请");
        }

        AfterSale afterSale = new AfterSale();
        afterSale.setOrderId(orderId);
        afterSale.setOrderItemId(orderItemId);
        afterSale.setUserId(userId);
        afterSale.setMerchantId(order.getMerchantId());
        afterSale.setType(type);
        afterSale.setReason(reason);
        afterSale.setDescription(description);
        afterSale.setImages(images);
        afterSale.setRefundAmount(refundAmount != null ? refundAmount : order.getPayAmount());
        afterSale.setStatus(0); // 待处理
        afterSaleMapper.insert(afterSale);

        // 申请售后后，将订单状态从“待评价”或“已完成”改为“已完成”（不再显示待评价按钮）
        // 这里只是关闭评价入口，不影响售后流程本身
        if (order.getOrderStatus() != null && order.getOrderStatus() == 3) {
            order.setOrderStatus(4); // 已完成
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }

        return afterSale;
    }

    @Override
    public List<AfterSale> getUserAfterSales() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        QueryWrapper<AfterSale> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        List<AfterSale> list = afterSaleMapper.selectList(wrapper);
        
        // 填充订单号与首个商品图
        for (AfterSale afterSale : list) {
            Order order = orderMapper.selectById(afterSale.getOrderId());
            if (order != null) {
                afterSale.setOrderNo(order.getOrderNo());
            }
            if (afterSale.getOrderItemId() != null) {
                com.fruitshop.entity.OrderItem item = orderItemMapper.selectById(afterSale.getOrderItemId());
                if (item != null) {
                    afterSale.setProductName(item.getProductName());
                    afterSale.setProductImage(item.getProductImage());
                }
            } else {
                QueryWrapper<com.fruitshop.entity.OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper.eq("order_id", afterSale.getOrderId());
                itemWrapper.last("LIMIT 1");
                com.fruitshop.entity.OrderItem item = orderItemMapper.selectOne(itemWrapper);
                if (item != null) {
                    afterSale.setProductName(item.getProductName());
                    afterSale.setProductImage(item.getProductImage());
                }
            }
        }
        return list;
    }

    @Override
    public List<AfterSale> getMerchantAfterSales(Integer status) {
        Long merchantId = UserContext.getUserId(); // 商家登录后的ID
        QueryWrapper<AfterSale> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        List<AfterSale> list = afterSaleMapper.selectList(wrapper);
        
        // 填充订单号与首个商品图
        for (AfterSale afterSale : list) {
            Order order = orderMapper.selectById(afterSale.getOrderId());
            if (order != null) {
                afterSale.setOrderNo(order.getOrderNo());
            }
            if (afterSale.getOrderItemId() != null) {
                com.fruitshop.entity.OrderItem item = orderItemMapper.selectById(afterSale.getOrderItemId());
                if (item != null) {
                    afterSale.setProductName(item.getProductName());
                    afterSale.setProductImage(item.getProductImage());
                }
            } else {
                QueryWrapper<com.fruitshop.entity.OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper.eq("order_id", afterSale.getOrderId());
                itemWrapper.last("LIMIT 1");
                com.fruitshop.entity.OrderItem item = orderItemMapper.selectOne(itemWrapper);
                if (item != null) {
                    afterSale.setProductName(item.getProductName());
                    afterSale.setProductImage(item.getProductImage());
                }
            }
        }
        return list;
    }

    @Override
    @Transactional
    public void processAfterSale(Long id, boolean agree, String reply) {
        AfterSale afterSale = afterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw new RuntimeException("售后申请不存在");
        }
        if (afterSale.getStatus() != 0) {
            throw new RuntimeException("该售后申请已处理");
        }

        // 0-待处理，1-商家已同意（退货退款待用户退货），2-商家已驳回，3-已完成（退款完成），4-用户已申诉
        if (agree) {
            // 区分仅退款与退货退款
            if (afterSale.getType() != null && afterSale.getType() == 2) {
                // 仅退款：商家同意后直接退款并标记为已完成
                afterSale.setStatus(3);
            } else {
                // 退货退款：先标记为“已同意”，等待用户退货
                afterSale.setStatus(1);
            }
        } else {
            afterSale.setStatus(2);
        }
        afterSale.setMerchantReply(reply);
        afterSale.setReplyTime(LocalDateTime.now());
        afterSale.setProcessTime(LocalDateTime.now());
        afterSaleMapper.updateById(afterSale);

        // 仅退款 或 后续确认退货 时才更新订单支付状态为“已退款”。
        if (agree && afterSale.getType() != null && afterSale.getType() == 2) {
            Order order = orderMapper.selectById(afterSale.getOrderId());
            if (order != null) {
                order.setPaymentStatus(3); // 已退款
                orderMapper.updateById(order);
            }
        }
    }

    @Override
    @Transactional
    public void confirmReturn(Long id, String logisticsCompany, String logisticsNo) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        AfterSale afterSale = afterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw new RuntimeException("售后申请不存在");
        }
        if (!userId.equals(afterSale.getUserId())) {
            throw new RuntimeException("无权操作该售后申请");
        }
        if (afterSale.getType() == null || afterSale.getType() != 1) {
            throw new RuntimeException("该售后单不是退货退款类型");
        }
        if (afterSale.getStatus() == null || afterSale.getStatus() != 1) {
            throw new RuntimeException("当前状态不支持确认退货");
        }

        // 用户确认已退货 -> 标记为已完成并执行退款
        if (logisticsCompany == null || logisticsCompany.trim().isEmpty()) {
            throw new RuntimeException("请填写退货物流公司");
        }
        if (logisticsNo == null || logisticsNo.trim().isEmpty()) {
            throw new RuntimeException("请填写退货物流单号");
        }
        afterSale.setReturnLogisticsCompany(logisticsCompany.trim());
        afterSale.setReturnLogisticsNo(logisticsNo.trim());
        afterSale.setStatus(3);
        afterSale.setReturnLogisticsTime(LocalDateTime.now());
        afterSale.setUpdateTime(LocalDateTime.now());
        afterSaleMapper.updateById(afterSale);

        Order order = orderMapper.selectById(afterSale.getOrderId());
        if (order != null) {
            order.setPaymentStatus(3); // 已退款
            orderMapper.updateById(order);
        }
    }

    @Override
    @Transactional
    public void appealAfterSale(Long id, String appealReason, String images) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        AfterSale afterSale = afterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw new RuntimeException("售后申请不存在");
        }
        if (!userId.equals(afterSale.getUserId())) {
            throw new RuntimeException("无权操作该售后申请");
        }
        if (afterSale.getStatus() == null || afterSale.getStatus() != 2) {
            throw new RuntimeException("只有被商家驳回的售后单才能申诉");
        }
        if (images == null || images.trim().isEmpty()) {
            throw new RuntimeException("请至少上传一张申诉图片");
        }

        // 状态改为“用户已申诉”，说明里追加申诉内容，方便管理员查看
        afterSale.setStatus(4);
        if (appealReason != null && !appealReason.trim().isEmpty()) {
            String desc = afterSale.getDescription();
            if (desc == null) desc = "";
            String appended = "【用户申诉】" + appealReason.trim();
            afterSale.setDescription(desc.isEmpty() ? appended : (desc + "\n" + appended));
        }

        // 追加申诉图片到 images 字段，方便管理员查看
        if (images != null && !images.trim().isEmpty()) {
            String existingImages = afterSale.getImages();
            if (existingImages == null || existingImages.trim().isEmpty()) {
                afterSale.setImages(images.trim());
            } else {
                afterSale.setImages(existingImages + "," + images.trim());
            }
        }
        afterSale.setUpdateTime(LocalDateTime.now());
        afterSaleMapper.updateById(afterSale);
    }

    @Override
    public AfterSale getAfterSaleDetail(Long id) {
        AfterSale afterSale = afterSaleMapper.selectById(id);
        if (afterSale != null) {
            Order order = orderMapper.selectById(afterSale.getOrderId());
            if (order != null) {
                afterSale.setOrderNo(order.getOrderNo());
            }
        }
        return afterSale;
    }

    @Override
    public List<AfterSale> getAdminAppeals(Integer status) {
        QueryWrapper<AfterSale> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        List<AfterSale> list = afterSaleMapper.selectList(wrapper);

        for (AfterSale afterSale : list) {
            Order order = orderMapper.selectById(afterSale.getOrderId());
            if (order != null) {
                afterSale.setOrderNo(order.getOrderNo());
            }
            if (afterSale.getOrderItemId() != null) {
                com.fruitshop.entity.OrderItem item = orderItemMapper.selectById(afterSale.getOrderItemId());
                if (item != null) {
                    afterSale.setProductName(item.getProductName());
                    afterSale.setProductImage(item.getProductImage());
                }
            } else {
                QueryWrapper<com.fruitshop.entity.OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper.eq("order_id", afterSale.getOrderId());
                itemWrapper.last("LIMIT 1");
                com.fruitshop.entity.OrderItem item = orderItemMapper.selectOne(itemWrapper);
                if (item != null) {
                    afterSale.setProductName(item.getProductName());
                    afterSale.setProductImage(item.getProductImage());
                }
            }
        }
        return list;
    }

    @Override
    @Transactional
    public void adminProcessAppeal(Long id, boolean approve, String remark) {
        AfterSale afterSale = afterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw new RuntimeException("售后申请不存在");
        }
        if (afterSale.getStatus() == null || afterSale.getStatus() != 4) {
            throw new RuntimeException("当前售后单不在申诉处理中状态");
        }

        String desc = afterSale.getDescription();
        if (desc == null) desc = "";
        String prefix = "【管理员处理结果】";
        String content = (remark != null && !remark.trim().isEmpty()) ? remark.trim() : (approve ? "同意退款" : "驳回申诉，维持商家原处理结果");
        String appended = prefix + content;
        afterSale.setDescription(desc.isEmpty() ? appended : (desc + "\n" + appended));

        if (approve) {
            // 管理员强制同意退款：直接标记为已完成并退款
            afterSale.setStatus(3);
            afterSale.setUpdateTime(LocalDateTime.now());
            afterSaleMapper.updateById(afterSale);

            Order order = orderMapper.selectById(afterSale.getOrderId());
            if (order != null) {
                order.setPaymentStatus(3); // 已退款
                orderMapper.updateById(order);
            }
        } else {
            // 管理员驳回申诉，最终状态回到“商家已驳回”
            afterSale.setStatus(2);
            afterSale.setUpdateTime(LocalDateTime.now());
            afterSaleMapper.updateById(afterSale);
        }
    }
}

