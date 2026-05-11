package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.ReviewLike;
import com.fruitshop.mapper.ReviewLikeMapper;
import com.fruitshop.service.ReviewLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReviewLikeServiceImpl extends ServiceImpl<ReviewLikeMapper, ReviewLike> implements ReviewLikeService {
    @Autowired
    private ReviewLikeMapper reviewLikeMapper;

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long reviewId, Integer type) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        // 查找是否已有点赞记录
        QueryWrapper<ReviewLike> wrapper = new QueryWrapper<>();
        wrapper.eq("review_id", reviewId);
        wrapper.eq("user_id", userId);
        ReviewLike existingLike = reviewLikeMapper.selectOne(wrapper);

        if (existingLike != null) {
            // 如果已有点赞记录
            if (existingLike.getType().equals(type)) {
                // 如果点击的是同类型，则取消点赞
                reviewLikeMapper.deleteById(existingLike.getId());
            } else {
                // 如果点击的是不同类型，则更新类型
                existingLike.setType(type);
                reviewLikeMapper.updateById(existingLike);
            }
        } else {
            // 如果没有点赞记录，创建新的
            ReviewLike reviewLike = new ReviewLike();
            reviewLike.setReviewId(reviewId);
            reviewLike.setUserId(userId);
            reviewLike.setType(type);
            reviewLikeMapper.insert(reviewLike);
        }

        // 返回最新的点赞数和不认同数
        QueryWrapper<ReviewLike> likeWrapper = new QueryWrapper<>();
        likeWrapper.eq("review_id", reviewId);
        likeWrapper.eq("type", 1);
        Long likeCountLong = reviewLikeMapper.selectCount(likeWrapper);
        int likeCount = likeCountLong != null ? likeCountLong.intValue() : 0;

        QueryWrapper<ReviewLike> dislikeWrapper = new QueryWrapper<>();
        dislikeWrapper.eq("review_id", reviewId);
        dislikeWrapper.eq("type", 2);
        Long dislikeCountLong = reviewLikeMapper.selectCount(dislikeWrapper);
        int dislikeCount = dislikeCountLong != null ? dislikeCountLong.intValue() : 0;

        // 获取当前用户的点赞状态
        QueryWrapper<ReviewLike> userLikeWrapper = new QueryWrapper<>();
        userLikeWrapper.eq("review_id", reviewId);
        userLikeWrapper.eq("user_id", userId);
        ReviewLike userLike = reviewLikeMapper.selectOne(userLikeWrapper);
        Integer userLikeType = userLike != null ? userLike.getType() : null;

        Map<String, Object> result = new HashMap<>();
        result.put("likeCount", likeCount);
        result.put("dislikeCount", dislikeCount);
        result.put("userLikeType", userLikeType);
        return result;
    }
}
