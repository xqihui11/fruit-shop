package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.Review;
import com.fruitshop.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/review")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @PostMapping("/create")
    public Result<Review> createReview(@RequestBody Map<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());
            Long productId = Long.valueOf(params.get("productId").toString());
            Integer rating = Integer.valueOf(params.get("rating").toString());
            String content = params.get("content") != null ? params.get("content").toString() : "";
            String images = params.get("images") != null ? params.get("images").toString() : null;
            Review review = reviewService.createReview(orderId, productId, rating, content, images);
            return Result.success("评价成功", review);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/product/{productId}")
    public Result<List<Review>> getProductReviews(@PathVariable Long productId) {
        try {
            List<Review> reviews = reviewService.getProductReviews(productId);
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/merchant/{merchantId}")
    public Result<Map<String, Object>> getMerchantReviews(@PathVariable Long merchantId) {
        try {
            List<Review> reviews = reviewService.getMerchantReviews(merchantId);
            Double avgRating = reviewService.getMerchantAverageRating(merchantId);
            Map<String, Object> result = new HashMap<>();
            result.put("reviews", reviews);
            result.put("averageRating", avgRating);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user")
    public Result<List<Review>> getUserReviews() {
        try {
            List<Review> reviews = reviewService.getUserReviews();
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reply/{id}")
    public Result<Void> replyReview(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String content = params.get("content") != null ? params.get("content").toString() : "";
            Long replyToId = null;
            if (params.get("replyToId") != null) {
                replyToId = Long.valueOf(params.get("replyToId").toString());
            }
            reviewService.replyReview(id, content, replyToId);
            return Result.success("回复成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

