package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.TicketManagementSystem.DamaiTicketing.Entity.Address;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "用户地址", description = "用户操作地址相关的所有操作接口")
public class AddressController {

    final
    AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    // 获取收获地址
    @SaCheckLogin
    @GetMapping("/api/address")
    @Operation(
            summary = "获取收获地址"
    )
    public Response getAddress() {
        return Response.success(200, "查询成功", addressService.getAddress());
    }

    // 修改收获地址
    @SaCheckLogin
    @PutMapping("/api/address/update")
    @Operation(
            summary = "修改收获地址"
    )
    public Response updateAddress(@RequestBody Address address) {
        addressService.updateAddress(address);
        return Response.success(200, "修改个人信息成功", addressService.getAddress());
    }

    // 删除收获地址
    @SaCheckLogin
    @DeleteMapping("/api/address/delete/{id}")
    @Operation(
            summary = "删除收获地址"
    )
    public Response deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return Response.success(200, "删除地址成功");
    }

}
