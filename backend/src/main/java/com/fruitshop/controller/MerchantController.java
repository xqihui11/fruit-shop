package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.Merchant;
import com.fruitshop.entity.Order;
import com.fruitshop.entity.OrderItem;
import com.fruitshop.entity.Product;
import com.fruitshop.entity.AfterSale;
import com.fruitshop.entity.Coupon;
import com.fruitshop.service.MerchantService;
import com.fruitshop.service.OrderService;
import com.fruitshop.service.ProductService;
import com.fruitshop.service.AfterSaleService;
import com.fruitshop.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/merchant")
public class MerchantController {
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AfterSaleService afterSaleService;
    @Autowired
    private CouponService couponService;

    // 消费者端接口
    @GetMapping("/list")
    public Result<List<Merchant>> getMerchantList() {
        try {
            List<Merchant> merchants = merchantService.getActiveMerchants();
            merchants.forEach(m -> m.setPassword(null));
            return Result.success(merchants);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public Result<Merchant> getMerchantDetail(@PathVariable Long id) {
        try {
            Merchant merchant = merchantService.getMerchantDetail(id);
            merchant.setPassword(null);
            return Result.success(merchant);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 商家端接口
    @PostMapping("/register")
    public Result<Merchant> register(@RequestBody Merchant merchant) {
        try {
            Merchant result = merchantService.register(merchant);
            result.setPassword(null);
            return Result.success("注册成功，请等待审核", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            String token = merchantService.login(username, password);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            Merchant merchant = merchantService.getCurrentMerchant();
            if (merchant != null) {
                merchant.setPassword(null);
            }
            data.put("merchant", merchant);
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<Merchant> getCurrentMerchant() {
        try {
            Merchant merchant = merchantService.getCurrentMerchant();
            if (merchant == null) {
                return Result.error(401, "未登录");
            }
            merchant.setPassword(null);
            return Result.success(merchant);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Merchant> updateMerchant(@RequestBody Merchant merchant) {
        try {
            Merchant result = merchantService.updateMerchant(merchant);
            result.setPassword(null);
            return Result.success("更新成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = merchantService.getStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 商品管理
    @PostMapping("/product/add")
    public Result<Product> addProduct(@RequestBody Product product) {
        try {
            Product result = productService.addProduct(product);
            return Result.success("商品添加成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/product/update")
    public Result<Product> updateProduct(@RequestBody Product product) {
        try {
            Product result = productService.updateProduct(product);
            return Result.success("商品更新成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/product/status/{id}")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            Integer status = params.get("status");
            productService.updateProductStatus(id, status);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/product/list")
    public Result<List<Product>> getMerchantProducts(@RequestParam(required = false) Integer status) {
        try {
            List<Product> products = productService.getMerchantProducts(status);
            return Result.success(products);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 订单管理
    @GetMapping("/order/list")
    public Result<List<Order>> getMerchantOrders(@RequestParam(required = false) Integer status) {
        try {
            List<Order> orders = orderService.getMerchantOrders(status);
            return Result.success(orders);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈，便于调试
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/order/detail/{id}")
    public Result<Map<String, Object>> getMerchantOrderDetail(@PathVariable Long id) {
        try {
            Order order = orderService.getMerchantOrderDetail(id);
            List<OrderItem> items = orderService.getOrderItems(id);
            Map<String, Object> result = new HashMap<>();
            result.put("order", order);
            result.put("items", items);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/order/ship/{id}")
    public Result<Void> shipOrder(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String logisticsCompany = params.get("logisticsCompany");
            String logisticsNo = params.get("logisticsNo");
            orderService.shipOrder(id, logisticsCompany, logisticsNo);
            return Result.success("发货成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 售后管理
    @GetMapping("/aftersale/list")
    public Result<List<AfterSale>> getMerchantAfterSales(@RequestParam(required = false) Integer status) {
        try {
            List<AfterSale> list = afterSaleService.getMerchantAfterSales(status);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/aftersale/process/{id}")
    public Result<Void> processAfterSale(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            boolean agree = Boolean.parseBoolean(params.get("agree").toString());
            String reply = params.get("reply") != null ? params.get("reply").toString() : "";
            afterSaleService.processAfterSale(id, agree, reply);
            return Result.success("处理成功", null);
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
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/coupon/add")
    public Result<Coupon> addCoupon(@RequestBody Coupon coupon) {
        try {
            Coupon result = couponService.createCoupon(coupon);
            return Result.success("添加成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/coupon/update")
    public Result<Coupon> updateCoupon(@RequestBody Coupon coupon) {
        try {
            // 验证商家只能更新自己的优惠券
            Coupon existing = couponService.getById(coupon.getId());
            if (existing == null) {
                return Result.error("优惠券不存在");
            }
            Long merchantId = merchantService.getCurrentMerchant().getId();
            if (existing.getMerchantId() == null || !existing.getMerchantId().equals(merchantId)) {
                return Result.error("无权限修改此优惠券");
            }
            
            Coupon result = couponService.updateCoupon(coupon);
            return Result.success("更新成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/coupon/delete/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        try {
            // 验证商家只能删除自己的优惠券
            Coupon existing = couponService.getById(id);
            if (existing == null) {
                return Result.error("优惠券不存在");
            }
            Long merchantId = merchantService.getCurrentMerchant().getId();
            if (existing.getMerchantId() == null || !existing.getMerchantId().equals(merchantId)) {
                return Result.error("无权限删除此优惠券");
            }
            
            couponService.deleteCoupon(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/coupon/status/{id}")
    public Result<Void> updateCouponStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            // 验证商家只能更新自己的优惠券状态
            Coupon existing = couponService.getById(id);
            if (existing == null) {
                return Result.error("优惠券不存在");
            }
            Long merchantId = merchantService.getCurrentMerchant().getId();
            if (existing.getMerchantId() == null || !existing.getMerchantId().equals(merchantId)) {
                return Result.error("无权限修改此优惠券");
            }
            
            Integer status = params.get("status");
            couponService.updateCouponStatus(id, status);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

