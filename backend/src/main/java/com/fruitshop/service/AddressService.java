package com.fruitshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitshop.entity.Address;
import java.util.List;

public interface AddressService extends IService<Address> {
    List<Address> getAddressList();
    Address addAddress(Address address);
    Address updateAddress(Address address);
    void deleteAddress(Long id);
    void setDefaultAddress(Long id);
}

