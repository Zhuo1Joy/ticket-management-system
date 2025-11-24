package com.TicketManagementSystem.DamaiTicketing.Controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Entity.User;
import com.TicketManagementSystem.DamaiTicketing.Entity.UserDTO;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    // 获取当前用户信息
    @GetMapping("/api/user")
    @SaCheckLogin // Sa-Token怎么这么强 有种全部白学的感觉
    public Response getUSerInformation(){
        return Response.success(200, "操作成功", userService.getUserInformation());
    }

    // 修改个人信息
    @PostMapping("/api/user/update")
    @SaCheckLogin
    public Response updateUserInformation(@RequestBody UserDTO user) {
        userService.updateUserInformation(user);
        return Response.success(200, "修改成功", userService.updateUserInformation(user));
    }


}
