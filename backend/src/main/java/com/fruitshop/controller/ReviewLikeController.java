package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.service.ReviewLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/review/like")
public class ReviewLikeController {
    @Autowired
    private ReviewLikeService reviewLikeService;

    @PostMapping("/{reviewId}")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long reviewId, @RequestBody Map<String, Integer> params) {
        try {
            Integer type = params.get("type");
            if (type == null || (type != 1 && type != 2)) {
                return Result.error("点赞类型错误");
            }
            Map<String, Object> result = reviewLikeService.toggleLike(reviewId, type);
            return Result.success("操作成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
