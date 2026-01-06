package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Service.AdminUserService;
import com.TicketManagementSystem.DamaiTicketing.Util.StpAdminUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "用户管理", description = "管理员操作用户相关的所有接口")
@SaCheckPermission(value = "admin", type = StpAdminUtil.TYPE)
public class AdminUserController {

    @Autowired
    AdminUserService adminUserService;

    @GetMapping("/api/admin/user")
    @Operation(
            summary = "分页查询用户信息"
    )
    public Response getUser(@RequestParam(required = false, defaultValue = "1") int pageNum) {
        return Response.success(200, "查询用户信息成功", adminUserService.getUser(pageNum));
    }

    @GetMapping("/api/admin/user/{userId}")
    @Operation(
            summary = "查询用户详情"
    )
    public Response getUserDetails(@PathVariable Long userId) {
        return Response.success(200, "查询用户信息成功", adminUserService.getUserDetails(userId));
    }

    @PostMapping("/api/admin/user/password")
    @Operation(
            summary = "修改用户信息（重置用户密码）"
    )
    public Response updateUserInformation(@RequestParam Long userId) {
        adminUserService.updateUserInformation(userId);
        return Response.success(200, "修改用户信息（重置用户密码）");
    }

    @PostMapping("/api/admin/user/status")
    @Operation(
            summary = "启用/禁用用户"
    )
    public Response updateUserStatus(@RequestParam Long userId, @RequestParam int status) {
        adminUserService.updateUserStatus(userId, status);
        return Response.success(200, "修改用户状态成功");
    }

}
