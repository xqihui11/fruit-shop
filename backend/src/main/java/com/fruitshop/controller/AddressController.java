package com.fruitshop.controller;

import com.fruitshop.common.Result;
import com.fruitshop.entity.Address;
import com.fruitshop.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @GetMapping("/list")
    public Result<List<Address>> getAddressList() {
        try {
            List<Address> addresses = addressService.getAddressList();
            return Result.success(addresses);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Address> addAddress(@RequestBody Address address) {
        try {
            Address result = addressService.addAddress(address);
            return Result.success("添加成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Address> updateAddress(@RequestBody Address address) {
        try {
            Address result = addressService.updateAddress(address);
            return Result.success("更新成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        try {
            addressService.deleteAddress(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/setDefault/{id}")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        try {
            addressService.setDefaultAddress(id);
            return Result.success("设置成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

