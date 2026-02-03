package com.TicketManagementSystem.DamaiTicketing.Controller;

import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Service.AdminLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "管理端登录", description = "管理员登录相关的所有操作接口")
public class AdminLoginController {

    final
    AdminLoginService adminLoginService;

    public AdminLoginController(AdminLoginService adminLoginService) {
        this.adminLoginService = adminLoginService;
    }

    @PostMapping("/api/login/admin")
    @Operation(
            summary = "管理员登录"
    )
    public Response loginByPassword(@RequestParam String username, @RequestParam String password) {
        return Response.success(200, "管理员登录成功", adminLoginService.loginByPassword(username, password));
    }

    @GetMapping("/api/admin/logout")
    @Operation(
            summary = "管理员登出"
    )
    public Response logout() {
        return Response.success(200, "退出登录成功", adminLoginService.logout());
    }

}
