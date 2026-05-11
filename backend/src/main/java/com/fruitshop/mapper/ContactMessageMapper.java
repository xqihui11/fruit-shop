package com.fruitshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitshop.entity.ContactMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContactMessageMapper extends BaseMapper<ContactMessage> {
}

