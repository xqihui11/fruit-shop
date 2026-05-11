package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.Merchant;
import com.fruitshop.entity.Order;
import com.fruitshop.entity.OrderItem;
import com.fruitshop.entity.Product;
import com.fruitshop.entity.Review;
import com.fruitshop.entity.ReviewLike;
import com.fruitshop.entity.ReviewReply;
import com.fruitshop.entity.User;
import com.fruitshop.mapper.*;
import com.fruitshop.service.MessageService;
import com.fruitshop.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private ReviewReplyMapper reviewReplyMapper;
    @Autowired
    private ReviewLikeMapper reviewLikeMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired(required = false)
    private MessageService messageService;

    @Override
    @Transactional
    public Review createReview(Long orderId, Long productId, Integer rating, String content, String images) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 3) {
            throw new RuntimeException("订单状态不正确，无法评价");
        }

        // 检查是否已评价
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        wrapper.eq("product_id", productId);
        wrapper.eq("user_id", userId);
        if (reviewMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该商品已评价");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setMerchantId(order.getMerchantId());
        review.setProductId(productId);
        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        review.setStatus(1);
        reviewMapper.insert(review);

        // 更新订单状态为已完成
        order.setOrderStatus(4);
        orderMapper.updateById(order);

        return review;
    }

    @Override
    public List<Review> getProductReviews(Long productId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        wrapper.eq("status", 1);
        wrapper.orderByDesc("create_time");
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        // 填充用户名和所有回复
        for (Review review : reviews) {
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                review.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                String userAvatar = user.getAvatar();
                review.setAvatar(userAvatar); // 设置用户头像
                System.out.println("设置评论头像 - ReviewId: " + review.getId() + ", UserId: " + user.getId() + ", Avatar: " + userAvatar);
            } else {
                System.out.println("用户不存在 - ReviewId: " + review.getId() + ", UserId: " + review.getUserId());
            }
            
            // 填充订单信息
            if (review.getOrderId() != null) {
                Order order = orderMapper.selectById(review.getOrderId());
                if (order != null) {
                    review.setOrderNo(order.getOrderNo());
                    review.setOrderCreateTime(order.getCreateTime()); // 设置购买时间
                    
                    // 获取订单项信息（规格、数量等）
                    QueryWrapper<OrderItem> orderItemWrapper = new QueryWrapper<>();
                    orderItemWrapper.eq("order_id", review.getOrderId());
                    orderItemWrapper.eq("product_id", review.getProductId());
                    List<OrderItem> orderItems = orderItemMapper.selectList(orderItemWrapper);
                    if (orderItems != null && !orderItems.isEmpty()) {
                        OrderItem orderItem = orderItems.get(0);
                        review.setSpecName(orderItem.getSpecName());
                        review.setQuantity(orderItem.getQuantity());
                        // 优先使用订单项中的商品图片（快照）
                        if (orderItem.getProductImage() != null && !orderItem.getProductImage().isEmpty()) {
                            review.setProductImage(orderItem.getProductImage());
                        }
                    }
                }
            }
            
            // 如果还没有商品图片，从商品表获取
            if (review.getProductImage() == null || review.getProductImage().isEmpty()) {
                Product product = productMapper.selectById(review.getProductId());
                if (product != null && product.getMainImage() != null && !product.getMainImage().isEmpty()) {
                    review.setProductImage(product.getMainImage());
                }
            }
            
            // 填充点赞信息
            QueryWrapper<ReviewLike> likeWrapper = new QueryWrapper<>();
            likeWrapper.eq("review_id", review.getId());
            likeWrapper.eq("type", 1);
            Long likeCountLong = reviewLikeMapper.selectCount(likeWrapper);
            review.setLikeCount(likeCountLong != null ? likeCountLong.intValue() : 0);
            
            QueryWrapper<ReviewLike> dislikeWrapper = new QueryWrapper<>();
            dislikeWrapper.eq("review_id", review.getId());
            dislikeWrapper.eq("type", 2);
            Long dislikeCountLong = reviewLikeMapper.selectCount(dislikeWrapper);
            review.setDislikeCount(dislikeCountLong != null ? dislikeCountLong.intValue() : 0);
            
            // 获取当前用户的点赞状态（如果已登录）
            Long currentUserId = UserContext.getUserId();
            if (currentUserId != null) {
                QueryWrapper<ReviewLike> userLikeWrapper = new QueryWrapper<>();
                userLikeWrapper.eq("review_id", review.getId());
                userLikeWrapper.eq("user_id", currentUserId);
                ReviewLike userLike = reviewLikeMapper.selectOne(userLikeWrapper);
                if (userLike != null) {
                    review.setUserLikeType(userLike.getType());
                }
            }
            
            // 获取所有回复
            QueryWrapper<ReviewReply> replyWrapper = new QueryWrapper<>();
            replyWrapper.eq("review_id", review.getId());
            replyWrapper.orderByAsc("create_time");
            List<ReviewReply> replies = reviewReplyMapper.selectList(replyWrapper);
            
            // 填充回复者信息
            for (ReviewReply reply : replies) {
                if (reply.getReplyType() == 1 && reply.getMerchantId() != null) {
                    // 商家回复，获取商家名称
                    Merchant merchant = merchantMapper.selectById(reply.getMerchantId());
                    if (merchant != null) {
                        reply.setMerchantName(merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername());
                        // 商家头像
                        reply.setAvatar(merchant.getAvatar());
                    }
                } else if (reply.getReplyType() == 2 && reply.getUserId() != null) {
                    // 用户回复，获取用户名和头像
                    User replyUser = userMapper.selectById(reply.getUserId());
                    if (replyUser != null) {
                        reply.setUsername(replyUser.getNickname() != null ? replyUser.getNickname() : replyUser.getUsername());
                        String replyAvatar = replyUser.getAvatar();
                        reply.setAvatar(replyAvatar); // 设置用户头像
                        System.out.println("设置商品评价回复头像 - ReplyId: " + reply.getId() + ", UserId: " + replyUser.getId() + ", Avatar: " + replyAvatar);
                    }
                }
                
                // 填充回复目标信息
                if (reply.getReplyToId() != null) {
                    ReviewReply replyTo = reviewReplyMapper.selectById(reply.getReplyToId());
                    if (replyTo != null) {
                        if (replyTo.getReplyType() == 1 && replyTo.getMerchantId() != null) {
                            // 回复目标是商家
                            Merchant replyToMerchant = merchantMapper.selectById(replyTo.getMerchantId());
                            if (replyToMerchant != null) {
                                reply.setReplyToMerchantName(replyToMerchant.getShopName() != null ? replyToMerchant.getShopName() : replyToMerchant.getUsername());
                            }
                        } else if (replyTo.getReplyType() == 2 && replyTo.getUserId() != null) {
                            // 回复目标是用户
                            User replyToUser = userMapper.selectById(replyTo.getUserId());
                            if (replyToUser != null) {
                                reply.setReplyToUsername(replyToUser.getNickname() != null ? replyToUser.getNickname() : replyToUser.getUsername());
                            }
                        }
                    }
                }
            }
            
            review.setReplies(replies);
            // 为了兼容，保留merchantReply字段（取第一个商家回复）
            if (replies != null && !replies.isEmpty()) {
                ReviewReply firstMerchantReply = replies.stream()
                    .filter(r -> r.getReplyType() == 1)
                    .findFirst()
                    .orElse(null);
                if (firstMerchantReply != null) {
                    review.setMerchantReply(firstMerchantReply.getContent());
                }
            }
        }
        return reviews;
    }

    @Override
    public List<Review> getMerchantReviews(Long merchantId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        wrapper.eq("status", 1);
        wrapper.orderByDesc("create_time");
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        // 填充信息
        for (Review review : reviews) {
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                review.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                String userAvatar = user.getAvatar();
                review.setAvatar(userAvatar); // 设置用户头像
                System.out.println("设置店铺评价头像 - ReviewId: " + review.getId() + ", UserId: " + user.getId() + ", Avatar: " + userAvatar);
            }
            Product product = productMapper.selectById(review.getProductId());
            if (product != null) {
                review.setProductName(product.getName());
            }
            
            // 填充订单信息
            if (review.getOrderId() != null) {
                Order order = orderMapper.selectById(review.getOrderId());
                if (order != null) {
                    review.setOrderNo(order.getOrderNo());
                    review.setOrderCreateTime(order.getCreateTime()); // 设置购买时间
                    
                    // 获取订单项信息（规格、数量等）
                    QueryWrapper<OrderItem> orderItemWrapper = new QueryWrapper<>();
                    orderItemWrapper.eq("order_id", review.getOrderId());
                    orderItemWrapper.eq("product_id", review.getProductId());
                    List<OrderItem> orderItems = orderItemMapper.selectList(orderItemWrapper);
                    if (orderItems != null && !orderItems.isEmpty()) {
                        OrderItem orderItem = orderItems.get(0);
                        review.setSpecName(orderItem.getSpecName());
                        review.setQuantity(orderItem.getQuantity());
                        // 优先使用订单项中的商品图片（快照）
                        if (orderItem.getProductImage() != null && !orderItem.getProductImage().isEmpty()) {
                            review.setProductImage(orderItem.getProductImage());
                        }
                    }
                }
            }
            
            // 如果还没有商品图片，从商品表获取
            if (review.getProductImage() == null || review.getProductImage().isEmpty()) {
                if (product != null && product.getMainImage() != null && !product.getMainImage().isEmpty()) {
                    review.setProductImage(product.getMainImage());
                }
            }
            
            // 填充点赞信息
            QueryWrapper<ReviewLike> likeWrapper = new QueryWrapper<>();
            likeWrapper.eq("review_id", review.getId());
            likeWrapper.eq("type", 1);
            Long likeCountLong = reviewLikeMapper.selectCount(likeWrapper);
            review.setLikeCount(likeCountLong != null ? likeCountLong.intValue() : 0);
            
            QueryWrapper<ReviewLike> dislikeWrapper = new QueryWrapper<>();
            dislikeWrapper.eq("review_id", review.getId());
            dislikeWrapper.eq("type", 2);
            Long dislikeCountLong = reviewLikeMapper.selectCount(dislikeWrapper);
            review.setDislikeCount(dislikeCountLong != null ? dislikeCountLong.intValue() : 0);
            
            // 获取当前用户的点赞状态（如果已登录）
            Long currentUserId = UserContext.getUserId();
            if (currentUserId != null) {
                QueryWrapper<ReviewLike> userLikeWrapper = new QueryWrapper<>();
                userLikeWrapper.eq("review_id", review.getId());
                userLikeWrapper.eq("user_id", currentUserId);
                ReviewLike userLike = reviewLikeMapper.selectOne(userLikeWrapper);
                if (userLike != null) {
                    review.setUserLikeType(userLike.getType());
                }
            }
            
            // 获取所有回复
            QueryWrapper<ReviewReply> replyWrapper = new QueryWrapper<>();
            replyWrapper.eq("review_id", review.getId());
            replyWrapper.orderByAsc("create_time");
            List<ReviewReply> replies = reviewReplyMapper.selectList(replyWrapper);
            
            // 填充回复者信息
            for (ReviewReply reply : replies) {
                if (reply.getReplyType() == 1 && reply.getMerchantId() != null) {
                    // 商家回复，获取商家名称
                    Merchant merchant = merchantMapper.selectById(reply.getMerchantId());
                    if (merchant != null) {
                        reply.setMerchantName(merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername());
                        // 商家头像
                        reply.setAvatar(merchant.getAvatar());
                    }
                } else if (reply.getReplyType() == 2 && reply.getUserId() != null) {
                    // 用户回复，获取用户名和头像
                    User replyUser = userMapper.selectById(reply.getUserId());
                    if (replyUser != null) {
                        reply.setUsername(replyUser.getNickname() != null ? replyUser.getNickname() : replyUser.getUsername());
                        String replyAvatar = replyUser.getAvatar();
                        reply.setAvatar(replyAvatar); // 设置用户头像
                        System.out.println("设置商品评价回复头像 - ReplyId: " + reply.getId() + ", UserId: " + replyUser.getId() + ", Avatar: " + replyAvatar);
                    }
                }
                
                // 填充回复目标信息
                if (reply.getReplyToId() != null) {
                    ReviewReply replyTo = reviewReplyMapper.selectById(reply.getReplyToId());
                    if (replyTo != null) {
                        if (replyTo.getReplyType() == 1 && replyTo.getMerchantId() != null) {
                            // 回复目标是商家
                            Merchant replyToMerchant = merchantMapper.selectById(replyTo.getMerchantId());
                            if (replyToMerchant != null) {
                                reply.setReplyToMerchantName(replyToMerchant.getShopName() != null ? replyToMerchant.getShopName() : replyToMerchant.getUsername());
                            }
                        } else if (replyTo.getReplyType() == 2 && replyTo.getUserId() != null) {
                            // 回复目标是用户
                            User replyToUser = userMapper.selectById(replyTo.getUserId());
                            if (replyToUser != null) {
                                reply.setReplyToUsername(replyToUser.getNickname() != null ? replyToUser.getNickname() : replyToUser.getUsername());
                            }
                        }
                    }
                }
            }
            
            review.setReplies(replies);
            // 为了兼容，保留merchantReply字段（取第一个商家回复）
            if (replies != null && !replies.isEmpty()) {
                ReviewReply firstMerchantReply = replies.stream()
                    .filter(r -> r.getReplyType() == 1)
                    .findFirst()
                    .orElse(null);
                if (firstMerchantReply != null) {
                    review.setMerchantReply(firstMerchantReply.getContent());
                }
            }
        }
        return reviews;
    }

    @Override
    public List<Review> getUserReviews() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("status", 1);
        wrapper.orderByDesc("create_time");
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        // 填充商品名
        for (Review review : reviews) {
            Product product = productMapper.selectById(review.getProductId());
            if (product != null) {
                review.setProductName(product.getName());
            }
        }
        return reviews;
    }

    @Override
    public Double getMerchantAverageRating(Long merchantId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        wrapper.eq("status", 1);
        wrapper.select("AVG(rating) as rating");
        Review result = reviewMapper.selectOne(wrapper);
        return result != null && result.getRating() != null ? result.getRating().doubleValue() : 5.0;
    }

    @Override
    public void replyReview(Long reviewId, String content, Long replyToId) {
        Long currentUserId = UserContext.getUserId();
        String role = UserContext.getRole();
        if (currentUserId == null) {
            throw new RuntimeException("请先登录");
        }
        
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        
        // 如果指定了回复目标，验证回复目标是否存在
        if (replyToId != null) {
            ReviewReply replyTo = reviewReplyMapper.selectById(replyToId);
            if (replyTo == null || !replyTo.getReviewId().equals(reviewId)) {
                throw new RuntimeException("回复目标不存在");
            }
        }
        
        ReviewReply reply = new ReviewReply();
        reply.setReviewId(reviewId);
        reply.setContent(content);
        reply.setReplyToId(replyToId);
        
        if ("merchant".equals(role)) {
            // 商家回复
            if (!review.getMerchantId().equals(currentUserId)) {
                throw new RuntimeException("无权回复该评价");
            }
            reply.setMerchantId(currentUserId);
            reply.setUserId(null);
            reply.setReplyType(1);
            
            // 发送消息给评价用户，带上 reviewId 方便前端精确定位
            if (messageService != null) {
                Product product = productMapper.selectById(review.getProductId());
                String productName = product != null ? product.getName() : "商品";
                String title = "商家回复了您的评价";
                String replyContent = String.format("商家回复了您对 %s 的评价：%s", productName, content);
                String linkUrl = "/product/" + review.getProductId() + "?reviewId=" + reviewId;
                messageService.sendMessage(review.getUserId(), 2, title, replyContent, linkUrl);
            }
        } else {
            // 消费者回复 - 允许任何用户回复任何评价
            reply.setMerchantId(null);
            reply.setUserId(currentUserId);
            reply.setReplyType(2);
            
            // 发送消息给评价的原用户和商家（如果回复的不是自己的评价）
            if (messageService != null) {
                Product product = productMapper.selectById(review.getProductId());
                String productName = product != null ? product.getName() : "商品";
                
                // 如果回复的不是自己的评价，通知原评价用户
                if (!review.getUserId().equals(currentUserId)) {
                    String title = "有用户回复了您的评价";
                    String replyContent = String.format("有用户回复了您对 %s 的评价：%s", productName, content);
                    String linkUrl = "/product/" + review.getProductId() + "?reviewId=" + reviewId;
                    messageService.sendMessage(review.getUserId(), 3, title, replyContent, linkUrl);
                }
                
                // 通知商家（如果商家不是当前用户），同样带 reviewId 便于定位
                if (!review.getMerchantId().equals(currentUserId)) {
                    String title = "用户回复了评价";
                    String replyContent = String.format("用户回复了对 %s 的评价：%s", productName, content);
                    String linkUrl = "/merchant/reviews?reviewId=" + reviewId;
                    messageService.sendMessage(review.getMerchantId(), 3, title, replyContent, linkUrl);
                }
            }
        }
        
        reviewReplyMapper.insert(reply);
    }
}

