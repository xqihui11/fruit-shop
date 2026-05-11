package com.fruitshop.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fruitshop.common.Result;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.*;
import com.fruitshop.mapper.OrderItemMapper;
import com.fruitshop.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private UserService userService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AfterSaleService afterSaleService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ContactMessageService contactMessageService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private AnnouncementService announcementService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            String token = adminService.login(username, password);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<Admin> getCurrentAdmin() {
        try {
            Admin admin = adminService.getCurrentAdmin();
            if (admin == null) {
                return Result.error(401, "未登录");
            }
            admin.setPassword(null);
            return Result.success(admin);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/changePassword")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        try {
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            adminService.changePassword(oldPassword, newPassword);
            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = adminService.getPlatformStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 用户管理
    @GetMapping("/user/list")
    public Result<List<User>> getUserList(@RequestParam(required = false) String keyword) {
        try {
            List<User> users = userService.list();
            // 为管理员端用户管理页面补充统计信息（如订单数），并去掉敏感字段
            for (User u : users) {
                u.setPassword(null);
                // 累计订单数
                QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
                orderWrapper.eq("user_id", u.getId());
                long orderCount = orderService.count(orderWrapper);
                u.setOrderCount(orderCount);
            }
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/user/status/{id}")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            Integer status = params.get("status");
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            user.setStatus(status);
            userService.updateById(user);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 商家管理
    @GetMapping("/merchant/list")
    public Result<List<Merchant>> getMerchantList(@RequestParam(required = false) Integer status) {
        try {
            List<Merchant> merchants;
            if (status != null) {
                merchants = merchantService.lambdaQuery().eq(Merchant::getStatus, status).list();
            } else {
                merchants = merchantService.list();
            }

            for (Merchant m : merchants) {
                // 隐藏敏感字段
                m.setPassword(null);

                try {
                    // 商品数：统计该商家下的所有商品（不区分状态）
                    QueryWrapper<Product> productWrapper = new QueryWrapper<>();
                    productWrapper.eq("merchant_id", m.getId());
                    long productCount = productService.count(productWrapper);
                    m.setProductCount(productCount);

                    // 订单数：统计该商家的所有订单
                    QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
                    orderWrapper.eq("merchant_id", m.getId());
                    long orderCount = orderService.count(orderWrapper);
                    m.setOrderCount(orderCount);

                    // 最后活跃时间：取该商家最近一笔订单的创建时间
                    // 如果没有订单，lastLoginTime 保持为 null，前端会显示"从未有订单"
                    if (orderCount > 0) {
                        QueryWrapper<Order> lastOrderWrapper = new QueryWrapper<>();
                        lastOrderWrapper.eq("merchant_id", m.getId());
                        lastOrderWrapper.orderByDesc("create_time");
                        lastOrderWrapper.last("LIMIT 1");
                        Order lastOrder = orderService.getOne(lastOrderWrapper);
                        if (lastOrder != null && lastOrder.getCreateTime() != null) {
                            m.setLastLoginTime(lastOrder.getCreateTime());
                        }
                    } else {
                        // 明确设置为 null，确保前端能正确判断
                        m.setLastLoginTime(null);
                    }
                } catch (Exception e) {
                    // 如果查询出错，设置默认值，避免影响其他商家数据
                    m.setProductCount(0L);
                    m.setOrderCount(0L);
                    m.setLastLoginTime(null);
                }
            }

            return Result.success(merchants);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/merchant/pending")
    public Result<List<Merchant>> getPendingMerchants() {
        try {
            List<Merchant> merchants = merchantService.getPendingMerchants();
            merchants.forEach(m -> m.setPassword(null));
            return Result.success(merchants);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/merchant/audit/{id}")
    public Result<Void> auditMerchant(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            boolean approve = Boolean.parseBoolean(params.get("approve").toString());
            Long adminId = UserContext.getUserId();
            merchantService.auditMerchant(id, approve, adminId);
            return Result.success(approve ? "审核通过" : "审核拒绝", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/merchant/status/{id}")
    public Result<Void> updateMerchantStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            Integer status = params.get("status");
            Merchant merchant = merchantService.getById(id);
            if (merchant == null) {
                return Result.error("商家不存在");
            }
            merchant.setStatus(status);
            merchantService.updateById(merchant);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 商品管理
    @GetMapping("/product/list")
    public Result<List<Product>> getProductList(@RequestParam(required = false) Integer status) {
        try {
            List<Product> products = productService.getAdminProductList(status);
            for (Product p : products) {
                // 补充所属商家名称
                Merchant merchant = merchantService.getById(p.getMerchantId());
                if (merchant != null) {
                    p.setMerchantName(merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername());
                }

                // 补充评价数
                QueryWrapper<Review> reviewWrapper = new QueryWrapper<>();
                reviewWrapper.eq("product_id", p.getId());
                reviewWrapper.eq("status", 1);
                long reviewCount = reviewService.count(reviewWrapper);
                p.setReviewCount(reviewCount);
            }
            return Result.success(products);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/product/pending")
    public Result<List<Product>> getPendingProducts() {
        try {
            List<Product> products = productService.getPendingProducts();
            return Result.success(products);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/product/audit/{id}")
    public Result<Void> auditProduct(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            boolean approve = Boolean.parseBoolean(params.get("approve").toString());
            String remark = params.get("remark") != null ? params.get("remark").toString() : "";
            Long adminId = UserContext.getUserId();
            productService.auditProduct(id, approve, remark, adminId);
            return Result.success(approve ? "审核通过" : "审核拒绝", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/product/status/{id}")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            Integer status = params.get("status");
            if (status == null || (status != 1 && status != 2)) {
                return Result.error("状态参数错误，仅支持上架(1)或下架(2)");
            }
            Product product = productService.getById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            product.setStatus(status);
            productService.updateById(product);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 分类管理
    @PostMapping("/category/add")
    public Result<Category> addCategory(@RequestBody Category category) {
        try {
            Category result = categoryService.addCategory(category);
            return Result.success("添加成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/category/update")
    public Result<Category> updateCategory(@RequestBody Category category) {
        try {
            Category result = categoryService.updateCategory(category);
            return Result.success("更新成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/category/delete/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 订单管理
    @GetMapping("/order/list")
    public Result<List<Order>> getOrderList(@RequestParam(required = false) Integer status) {
        try {
            List<Order> orders = orderService.getAdminOrderList(status);
            for (Order order : orders) {
                // 补充商品信息
                QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper.eq("order_id", order.getId());
                List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
                if (items != null && !items.isEmpty()) {
                    OrderItem first = items.get(0);
                    // 商品名称/图片兜底：优先订单快照，其次商品表
                    String firstName = first.getProductName();
                    String firstImage = first.getProductImage();
                    if ((firstName == null || firstName.trim().isEmpty()) && first.getProductId() != null) {
                        Product product = productService.getById(first.getProductId());
                        if (product != null) {
                            firstName = product.getName();
                            if (firstImage == null || firstImage.trim().isEmpty()) {
                                firstImage = product.getMainImage();
                            }
                        }
                    }
                    order.setFirstProductName(firstName);
                    order.setFirstProductImage(firstImage);
                    int totalQty = 0;
                    for (OrderItem item : items) {
                        totalQty += item.getQuantity() != null ? item.getQuantity() : 0;
                    }
                    order.setTotalQuantity(totalQty);
                }

                // 补充收货人信息
                if (order.getAddressId() != null) {
                    Address address = addressService.getById(order.getAddressId());
                    if (address != null) {
                        order.setReceiverName(address.getReceiverName());
                        order.setReceiverPhone(address.getReceiverPhone());
                    }
                }

                // 补充用户/商家名称
                if (order.getUserId() != null) {
                    User user = userService.getById(order.getUserId());
                    if (user != null) {
                        order.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    }
                }
                if (order.getMerchantId() != null) {
                    Merchant merchant = merchantService.getById(order.getMerchantId());
                    if (merchant != null) {
                        order.setMerchantName(merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername());
                    }
                }

                // 售后中标签（有未完成售后即标记为 true）
                QueryWrapper<AfterSale> afterWrapper = new QueryWrapper<>();
                afterWrapper.eq("order_id", order.getId());
                afterWrapper.ne("status", 3);
                order.setAfterSaleInProgress(afterSaleService.count(afterWrapper) > 0);
            }
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/order/action/{id}")
    public Result<Void> orderAction(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String action = params.get("action");
            Order order = orderService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            if ("ship".equals(action)) {
                if (order.getOrderStatus() != 1) return Result.error("仅待发货订单可发货");
                order.setOrderStatus(2);
                order.setShipTime(java.time.LocalDateTime.now());
                if (order.getLogisticsCompany() == null) order.setLogisticsCompany("平台配送");
                if (order.getLogisticsNo() == null) order.setLogisticsNo("ADMIN-" + order.getId());
            } else if ("confirm".equals(action)) {
                if (order.getOrderStatus() != 2 && order.getOrderStatus() != 3) {
                    return Result.error("仅待收货/待评价订单可确认完成");
                }
                order.setOrderStatus(4);
                order.setCompleteTime(java.time.LocalDateTime.now());
            } else if ("cancel".equals(action)) {
                if (order.getOrderStatus() == 4 || order.getOrderStatus() == 5) {
                    return Result.error("已完成/已取消订单不可取消");
                }
                order.setOrderStatus(5);
                order.setCancelTime(java.time.LocalDateTime.now());
                String reason = params.get("reason");
                order.setCancelReason(reason != null && !reason.trim().isEmpty() ? reason : "管理员取消订单");
            } else {
                return Result.error("不支持的操作");
            }
            orderService.updateById(order);
            return Result.success("操作成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 售后申诉管理
    @GetMapping("/aftersale/appeals")
    public Result<List<AfterSale>> getAfterSaleAppeals(@RequestParam(required = false) Integer status) {
        try {
            List<AfterSale> list = afterSaleService.getAdminAppeals(status);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/aftersale/appeals/process/{id}")
    public Result<Void> processAfterSaleAppeal(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            boolean approve = Boolean.parseBoolean(params.get("approve").toString());
            String remark = params.get("remark") != null ? params.get("remark").toString() : "";
            afterSaleService.adminProcessAppeal(id, approve, remark);
            return Result.success(approve ? "已同意退款" : "已驳回申诉", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 在线留言管理
    @GetMapping("/contact/messages")
    public Result<List<ContactMessage>> getContactMessages() {
        try {
            List<ContactMessage> messages = contactMessageService.getAllMessages();
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/contact/message/{id}")
    public Result<ContactMessage> getContactMessageById(@PathVariable Long id) {
        try {
            ContactMessage message = contactMessageService.getMessageById(id);
            return Result.success(message);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/contact/message/{id}")
    public Result<Void> updateContactMessage(@PathVariable Long id, @RequestBody ContactMessage contactMessage) {
        try {
            contactMessageService.updateMessageStatus(id, contactMessage.getStatus(), contactMessage.getReply());
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 优惠券管理
    @GetMapping("/coupon/list")
    public Result<List<Coupon>> getCouponList() {
        try {
            List<Coupon> coupons = couponService.getAllCoupons();
            return Result.success(coupons);
        } catch (Exception e) {
            e.printStackTrace(); // 打印错误堆栈，便于调试
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/coupon/add")
    public Result<Coupon> addCoupon(@RequestBody Coupon coupon) {
        try {
            Coupon result = couponService.createCoupon(coupon);
            return Result.success("添加成功", result);
        } catch (Exception e) {
            e.printStackTrace(); // 打印错误堆栈，便于调试
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/coupon/update")
    public Result<Coupon> updateCoupon(@RequestBody Coupon coupon) {
        try {
            Coupon result = couponService.updateCoupon(coupon);
            return Result.success("更新成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/coupon/delete/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/coupon/status/{id}")
    public Result<Void> updateCouponStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            Integer status = params.get("status");
            couponService.updateCouponStatus(id, status);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 公告管理
    @GetMapping("/announcement/list")
    public Result<List<Announcement>> getAnnouncementList() {
        try {
            List<Announcement> announcements = announcementService.getAllAnnouncements();
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/announcement/add")
    public Result<Announcement> addAnnouncement(@RequestBody Announcement announcement) {
        try {
            Announcement result = announcementService.createAnnouncement(announcement);
            return Result.success("添加成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/announcement/update")
    public Result<Announcement> updateAnnouncement(@RequestBody Announcement announcement) {
        try {
            Announcement result = announcementService.updateAnnouncement(announcement);
            return Result.success("更新成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/announcement/delete/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/announcement/publish/{id}")
    public Result<Void> publishAnnouncement(@PathVariable Long id) {
        try {
            announcementService.publishAnnouncement(id);
            return Result.success("发布成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/announcement/unpublish/{id}")
    public Result<Void> unpublishAnnouncement(@PathVariable Long id) {
        try {
            announcementService.unpublishAnnouncement(id);
            return Result.success("下线成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

