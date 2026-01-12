package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Entity.UserDTO;
import com.TicketManagementSystem.DamaiTicketing.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "用户信息", description = "用户个人信息相关的所有操作接口")
public class UserController {

    final
    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 获取当前用户信息
    @GetMapping("/api/user")
    @SaCheckLogin // Sa-Token怎么这么强 有种全部白学的感觉
    @Operation(
            summary = "获取当前用户信息"
    )
    public Response getUSerInformation(){
        return Response.success(200, "查询成功", userService.getUserInformation());
    }

    // 修改个人信息
    @PutMapping("/api/user/update")
    @SaCheckLogin
    @Operation(
            summary = "修改个人信息"
    )
    public Response updateUserInformation(@RequestBody UserDTO user) {
        userService.updateUserInformation(user);
        return Response.success(200, "修改个人信息成功", userService.getUserInformation());
    }

    // 修改密码
    @PutMapping("/api/user/password")
    @SaCheckLogin
    @Operation(
            summary = "修改密码"
    )
    public Response updateUserPassword(@RequestParam String code, @RequestParam String password) {
        userService.updateUserPassword(code, password);
        return Response.success(200, "修改密码成功，请重新登录");
    }

}
