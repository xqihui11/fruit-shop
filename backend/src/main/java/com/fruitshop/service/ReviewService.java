package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Review;

import java.util.List;

public interface ReviewService extends IService<Review> {
    Review createReview(Long orderId, Long productId, Integer rating, String content, String images);
    List<Review> getProductReviews(Long productId);
    List<Review> getMerchantReviews(Long merchantId);
    List<Review> getUserReviews();
    Double getMerchantAverageRating(Long merchantId);
    void replyReview(Long reviewId, String content, Long replyToId);
}

