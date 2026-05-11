package com.fruitshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitshop.common.UserContext;
import com.fruitshop.entity.Address;
import com.fruitshop.mapper.AddressMapper;
import com.fruitshop.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {
    @Override
    public List<Address> getAddressList() {
        Long userId = UserContext.getUserId();
        QueryWrapper<Address> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("is_default");
        wrapper.orderByDesc("create_time");
        return list(wrapper);
    }

    @Override
    public Address addAddress(Address address) {
        Long userId = UserContext.getUserId();
        address.setUserId(userId);
        if (address.getIsDefault() == 1) {
            // 取消其他默认地址
            UpdateWrapper<Address> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId);
            updateWrapper.set("is_default", 0);
            update(updateWrapper);
        }
        save(address);
        return address;
    }

    @Override
    public Address updateAddress(Address address) {
        Long userId = UserContext.getUserId();
        address.setUserId(userId);
        if (address.getIsDefault() == 1) {
            UpdateWrapper<Address> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId);
            updateWrapper.ne("id", address.getId());
            updateWrapper.set("is_default", 0);
            update(updateWrapper);
        }
        updateById(address);
        return address;
    }

    @Override
    public void deleteAddress(Long id) {
        removeById(id);
    }

    @Override
    public void setDefaultAddress(Long id) {
        Long userId = UserContext.getUserId();
        UpdateWrapper<Address> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId);
        updateWrapper.set("is_default", 0);
        update(updateWrapper);

        updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.set("is_default", 1);
        update(updateWrapper);
    }
}

