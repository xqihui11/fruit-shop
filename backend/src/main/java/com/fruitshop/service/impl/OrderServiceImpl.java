package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.*;
import com.fruitshop.mapper.*;
import com.fruitshop.entity.Coupon;
import com.fruitshop.service.MessageService;
import com.fruitshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartItemMapper cartItemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductSpecMapper productSpecMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired(required = false)
    private MessageService messageService;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    @Transactional
    public Order createOrder(Long addressId, String remark, Long couponId) {
        // 下单主流程（事务内）：
        // 1) 校验用户与地址 -> 2) 读取已勾选购物车 -> 3) 计算金额/优惠 ->
        // 4) 创建订单与订单项 -> 5) 标记优惠券已使用 -> 6) 清理购物车
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("收货地址不存在");
        }

        // 获取购物车选中商品
        QueryWrapper<CartItem> cartWrapper = new QueryWrapper<>();
        cartWrapper.eq("user_id", userId);
        cartWrapper.eq("is_selected", 1);
        List<CartItem> cartItems = cartItemMapper.selectList(cartWrapper);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        // 计算总金额并获取商家ID
        BigDecimal totalAmount = BigDecimal.ZERO;
        Long merchantId = null;
        for (CartItem cartItem : cartItems) {
            Product product = productMapper.selectById(cartItem.getProductId());
            if (product == null || product.getStatus() != 1) {
                throw new RuntimeException("商品不存在或已下架");
            }
            // 获取商家ID（从第一个商品获取，假设购物车中所有商品来自同一商家）
            if (merchantId == null) {
                merchantId = product.getMerchantId();
            } else if (!merchantId.equals(product.getMerchantId())) {
                throw new RuntimeException("购物车中的商品必须来自同一商家");
            }

            // 计算当前购物车项的单价：优先使用规格价格，其次使用商品价格
            BigDecimal price = product.getPrice();
            if (cartItem.getSpecId() != null) {
                com.fruitshop.entity.ProductSpec spec = productSpecMapper.selectById(cartItem.getSpecId());
                if (spec != null) {
                    // 保护：确保规格属于当前商品
                    if (spec.getProductId() != null && !spec.getProductId().equals(product.getId())) {
                        throw new RuntimeException("规格与商品不匹配");
                    }
                    if (spec.getPrice() != null) {
                        price = spec.getPrice();
                    }
                }
            }
            int payableQty = getPayableQuantity(product, cartItem.getQuantity());
            totalAmount = totalAmount.add(price.multiply(new BigDecimal(payableQty)));
        }
        
        if (merchantId == null) {
            throw new RuntimeException("无法获取商家信息");
        }

        // 处理优惠券
        BigDecimal discountAmount = BigDecimal.ZERO;
        Long userCouponId = null;
        if (couponId != null) {
            QueryWrapper<com.fruitshop.entity.UserCoupon> userCouponWrapper = new QueryWrapper<>();
            userCouponWrapper.eq("user_id", userId);
            userCouponWrapper.eq("coupon_id", couponId);
            userCouponWrapper.eq("status", 0); // 未使用
            com.fruitshop.entity.UserCoupon userCoupon = userCouponMapper.selectOne(userCouponWrapper);
            if (userCoupon == null) {
                throw new RuntimeException("优惠券不存在或已使用");
            }
            
            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null) {
                throw new RuntimeException("优惠券不存在");
            }
            
            // 检查优惠券使用范围：商家优惠券只能用于对应商家
            if (coupon.getMerchantId() != null) {
                // 这是商家优惠券，只能用于该商家
                if (!coupon.getMerchantId().equals(merchantId)) {
                    throw new RuntimeException("该优惠券仅限指定商家使用");
                }
            }
            // 如果merchantId为null，说明是平台优惠券，全平台可用，不需要验证
            
            // 检查是否满足使用条件
            if (totalAmount.compareTo(coupon.getMinAmount()) < 0) {
                throw new RuntimeException("订单金额未达到优惠券使用条件");
            }
            
            // 计算优惠金额
            if (coupon.getType() == 1) {
                // 满减券
                discountAmount = coupon.getDiscountAmount();
            } else {
                // 折扣券
                BigDecimal discount = totalAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountRate()));
                if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                    discountAmount = coupon.getMaxDiscountAmount();
                } else {
                    discountAmount = discount;
                }
            }
            
            userCouponId = userCoupon.getId();
        }
        
        BigDecimal payAmount = totalAmount.subtract(discountAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setOrderStatus(0); // 待支付
        order.setPaymentStatus(0);
        order.setRemark(remark);
        order.setMerchantId(merchantId);
        orderMapper.insert(order);
        
        // 如果使用了优惠券，更新优惠券状态
        if (userCouponId != null) {
            com.fruitshop.entity.UserCoupon userCoupon = new com.fruitshop.entity.UserCoupon();
            userCoupon.setId(userCouponId);
            userCoupon.setStatus(1); // 已使用
            userCoupon.setOrderId(order.getId());
            userCoupon.setUsedTime(LocalDateTime.now());
            userCouponMapper.updateById(userCoupon);
            
            // 更新优惠券使用数量
            Coupon coupon = couponMapper.selectById(couponId);
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponMapper.updateById(coupon);
        }

        // 创建订单项
        for (CartItem cartItem : cartItems) {
            Product product = productMapper.selectById(cartItem.getProductId());
            int paidQty = cartItem.getQuantity();
            int freeQty = getFreeCount(product, paidQty);
            int payableQty = getPayableQuantity(product, paidQty);

            // 订单项单价与规格信息
            BigDecimal unitPrice = product.getPrice();
            String specName = null;
            if (cartItem.getSpecId() != null) {
                com.fruitshop.entity.ProductSpec spec = productSpecMapper.selectById(cartItem.getSpecId());
                if (spec != null) {
                    if (spec.getProductId() != null && !spec.getProductId().equals(product.getId())) {
                        throw new RuntimeException("规格与商品不匹配");
                    }
                    if (spec.getPrice() != null) {
                        unitPrice = spec.getPrice();
                    }
                    specName = spec.getSpecName();
                }
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setSpecId(cartItem.getSpecId());
            orderItem.setSpecName(specName);
            orderItem.setPrice(unitPrice);
            orderItem.setQuantity(paidQty);
            orderItem.setSubtotal(unitPrice.multiply(new BigDecimal(payableQty)));
            orderItemMapper.insert(orderItem);

            // BUY_N_GET_M：赠品不计价，但需要在订单明细中体现出来
            if (freeQty > 0) {
                OrderItem freeOrderItem = new OrderItem();
                freeOrderItem.setOrderId(order.getId());
                freeOrderItem.setProductId(product.getId());
                freeOrderItem.setProductName(product.getName() + "(赠品)");
                freeOrderItem.setProductImage(product.getMainImage());
                freeOrderItem.setSpecId(cartItem.getSpecId());
                freeOrderItem.setSpecName(specName);
                freeOrderItem.setPrice(BigDecimal.ZERO);
                freeOrderItem.setQuantity(freeQty);
                freeOrderItem.setSubtotal(BigDecimal.ZERO);
                orderItemMapper.insert(freeOrderItem);
            }

            // 删除购物车项
            cartItemMapper.deleteById(cartItem.getId());
        }

        return order;
    }

    @Override
    public List<Order> getOrderList(Integer status) {
        Long userId = UserContext.getUserId();
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null) {
            wrapper.eq("order_status", status);
        }
        wrapper.orderByDesc("create_time");
        List<Order> list = list(wrapper);

        // 补充商家名称/头像 + 订单首个商品信息，方便前端“我的订单”和聊天卡片展示
        if (list != null && !list.isEmpty()) {
            java.util.Set<Long> merchantIds = new java.util.HashSet<>();
            for (Order o : list) {
                if (o.getMerchantId() != null) {
                    merchantIds.add(o.getMerchantId());
                }
            }
            if (!merchantIds.isEmpty()) {
                java.util.List<Merchant> merchants = merchantMapper.selectBatchIds(merchantIds);
                java.util.Map<Long, Merchant> merchantMap = new java.util.HashMap<>();
                for (Merchant m : merchants) {
                    merchantMap.put(m.getId(), m);
                }
                for (Order o : list) {
                    Merchant m = merchantMap.get(o.getMerchantId());
                    if (m != null) {
                        o.setMerchantName(m.getShopName());
                        o.setMerchantAvatar(m.getAvatar());
                    }
                }
            }

            // 为每个订单补充首个商品信息（用于订单卡片展示）
            for (Order o : list) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderItem> itemWrapper =
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                itemWrapper.eq("order_id", o.getId());
                itemWrapper.last("LIMIT 1");
                OrderItem firstItem = orderItemMapper.selectOne(itemWrapper);
                if (firstItem != null) {
                    o.setFirstProductName(firstItem.getProductName());
                    o.setFirstProductImage(firstItem.getProductImage());
                }
            }
        }

        return list;
    }

    @Override
    public Order getOrderDetail(Long id) {
        Long userId = UserContext.getUserId();
        Order order = getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    @Override
    public void cancelOrder(Long id, String reason) {
        Order order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("只能取消待支付订单");
        }
        order.setOrderStatus(5); // 已取消
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        updateById(order);
    }

    @Override
    public void confirmReceive(Long id) {
        Order order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            throw new RuntimeException("订单状态不正确");
        }
        order.setOrderStatus(3); // 待评价
        order.setReceiveTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public List<Order> getMerchantOrders(Integer status) {
        Long merchantId = UserContext.getUserId();
        if (merchantId == null) {
            throw new RuntimeException("请先登录");
        }
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        if (status != null) {
            wrapper.eq("order_status", status);
        }
        wrapper.orderByDesc("create_time");
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders;
    }

    @Override
    public Order getMerchantOrderDetail(Long id) {
        Long merchantId = UserContext.getUserId();
        if (merchantId == null) {
            throw new RuntimeException("请先登录");
        }
        Order order = getById(id);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    @Override
    public void shipOrder(Long id, String logisticsCompany, String logisticsNo) {
        Long merchantId = UserContext.getUserId();
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("订单不存在或无权操作");
        }
        if (order.getOrderStatus() != 1) {
            throw new RuntimeException("订单状态不正确，无法发货");
        }
        order.setOrderStatus(2); // 待收货
        order.setShipTime(LocalDateTime.now());
        order.setLogisticsCompany(logisticsCompany);
        order.setLogisticsNo(logisticsNo);
        orderMapper.updateById(order);
        
        // 发送消息给用户（带上订单ID，方便前端直接定位到对应订单卡片/详情）
        if (messageService != null) {
            String title = "您的订单已发货";
            String content = String.format("您的订单 %s 已发货，物流公司：%s，物流单号：%s",
                order.getOrderNo(), logisticsCompany, logisticsNo);
            String linkUrl = "/order?orderId=" + order.getId();
            messageService.sendMessage(order.getUserId(), 1, title, content, linkUrl);
        }
    }

    @Override
    public List<Order> getAdminOrderList(Integer status) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("order_status", status);
        }
        wrapper.orderByDesc("create_time");
        return orderMapper.selectList(wrapper);
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        // 这个方法被用户端 / 商家端 / 管理端共用，需要按角色做可见性控制
        String role = UserContext.getRole();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 管理员可以查看任意订单的明细
        if ("admin".equals(role)) {
            // 不做用户/商家校验
        }
        // 如果是商家端，检查 merchantId
        else if ("merchant".equals(role)) {
            Long merchantId = UserContext.getUserId();
            if (!order.getMerchantId().equals(merchantId)) {
                throw new RuntimeException("订单不存在");
            }
        } else {
            // 用户端检查 userId
            Long userId = UserContext.getUserId();
            if (!order.getUserId().equals(userId)) {
                throw new RuntimeException("订单不存在");
            }
        }
        
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return orderItemMapper.selectList(wrapper);
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * 计算特价“买N送M”的应付数量。
     * 约定：购物车/下单传入的 `quantity` 表示“已购买（需要支付）的件数”；
     * 赠品件数单独按 order_items 写入（价格为0），不从应付数量中扣减。
     */
    private int getPayableQuantity(Product product, int quantity) {
        if (quantity <= 0) return 0;
        if (product == null) return quantity;
        if (product.getSpecialEnabled() == null || product.getSpecialEnabled() != 1) return quantity;
        if (product.getSpecialType() == null) return quantity;
        if (!"BUY_N_GET_M".equalsIgnoreCase(product.getSpecialType())) return quantity;
        return quantity;
    }

    /**
     * BUY_N_GET_M：计算赠品数量（不计价）。
     * 约定：paidQuantity 表示“已购买（需要支付）的件数”。
     * 例如 买2送1：paidQuantity=2 -> freeCount=1；paidQuantity=3 -> freeCount=1。
     */
    private int getFreeCount(Product product, int paidQuantity) {
        if (paidQuantity <= 0) return 0;
        if (product == null) return 0;
        if (product.getSpecialEnabled() == null || product.getSpecialEnabled() != 1) return 0;
        if (product.getSpecialType() == null) return 0;
        if (!"BUY_N_GET_M".equalsIgnoreCase(product.getSpecialType())) return 0;

        Integer buy = product.getSpecialBuy();
        Integer free = product.getSpecialFree();
        if (buy == null || buy <= 0 || free == null || free <= 0) return 0;

        return (paidQuantity / buy) * free;
    }

    /**
     * 自动取消超时未支付订单：每分钟扫描一次
     * 条件：order_status=0(待支付)、payment_status=0，创建时间早于当前时间10分钟
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void autoCancelUnpaidOrders() {
        // 定时任务用于兜底：避免“长期待支付”订单无限堆积。
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(10);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_status", 0);
        wrapper.eq("payment_status", 0);
        wrapper.lt("create_time", deadline);
        List<Order> overdueOrders = orderMapper.selectList(wrapper);
        if (overdueOrders == null || overdueOrders.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        for (Order order : overdueOrders) {
            order.setOrderStatus(5); // 已取消
            order.setCancelTime(now);
            order.setCancelReason("超时未支付，系统自动取消");
            order.setUpdateTime(now);
        }
        this.updateBatchById(overdueOrders);
    }

    /**
     * 自动完成超时未评价订单：每小时扫描一次
     * 条件：order_status=3(待评价)，收货时间早于当前时间24小时
     */
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void autoCompleteUnreviewedOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(24);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_status", 3);
        wrapper.lt("receive_time", deadline);
        List<Order> toComplete = orderMapper.selectList(wrapper);
        if (toComplete == null || toComplete.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        for (Order order : toComplete) {
            order.setOrderStatus(4); // 已完成
            order.setCompleteTime(now);
            order.setUpdateTime(now);
        }
        this.updateBatchById(toComplete);
    }
}


