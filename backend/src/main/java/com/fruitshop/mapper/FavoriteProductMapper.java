package com.fruitshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitshop.entity.FavoriteProduct;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteProductMapper extends BaseMapper<FavoriteProduct> {
}

