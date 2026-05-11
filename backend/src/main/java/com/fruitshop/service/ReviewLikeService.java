package com.fruitshop.service;

public interface ReviewLikeService {
    /**
     * 点赞或取消点赞
     * @param reviewId 评价ID
     * @param type 点赞类型：1-认同，2-不认同
     * @return 操作后的点赞数和不认同数
     */
    java.util.Map<String, Object> toggleLike(Long reviewId, Integer type);
}
