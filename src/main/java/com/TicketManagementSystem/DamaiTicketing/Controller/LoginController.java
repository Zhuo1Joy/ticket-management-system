package com.TicketManagementSystem.DamaiTicketing.Controller;

import com.TicketManagementSystem.DamaiTicketing.Entity.LoginRequest;
import com.TicketManagementSystem.DamaiTicketing.Entity.Response;
import com.TicketManagementSystem.DamaiTicketing.Service.LoginService;
/*
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;**/
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "认证模块", description = "登录相关的所有操作接口")
public class LoginController {

    @Autowired
    LoginService loginService;

    // 登录模块
    // 发送Token
    @PutMapping("/api/auth/login")
    @Operation(
            summary = "用户登录 返回Token"      // 接口的简短描述
    )
//    // @ApiResponses 注解：定义这个接口可能返回的各种响应
//    @ApiResponses({
//            // @ApiResponse 注解：描述一种具体的响应情况
//            @ApiResponse(
//                    responseCode = "200",           // HTTP状态码200表示成功
//                    description = "登录成功"  // 对这个状态码的描述
//            ),
//            @ApiResponse(
//                    responseCode = "401",           // HTTP状态码500表示服务器错误
//                    description = "登录失败"    // 错误描述
//            )
//    })
    public Response login(@RequestBody LoginRequest loginRequest) {
        String token = loginService.login(loginRequest);
        //if (token != null)
        return Response.success(200, token);
        // return Response.error(401, "用户名或密码错误");
        // 为什么我感觉根本不需要这个if 前面在LoginService抛了异常那应该直接结束了 不会再过这里吧
    }

    // 发送邮箱验证码
    @GetMapping("/api/auth/login/{email}")
    @Operation(
            summary = "发送邮箱验证码"      // 接口的简短描述
    )
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "验证码发送成功"
//            ),
//            @ApiResponse(
//                    responseCode = "400",
//                    description = "邮箱格式错误"
//            ),
//            @ApiResponse(
//                    responseCode = "500",
//                    description = "验证码发送失败"
//            )
//    })
    public Response sendEmailVerificationCode(@PathVariable String email) {
        loginService.sendEmailCode(email);
        return Response.success(200, "验证码发送成功");
    }

    // 退出登录
    @GetMapping("/api/auth/logout")
    @Operation(
            summary = "用户退出登录"
    )
//    @ApiResponse(
//            responseCode = "200",
//            description = "退出登录成功"
//    )
    public Response logout() {
        if(!loginService.logout()) return Response.error(404, "退出登录失败、请检查登录状态");
        return Response.success(200, "退出登录成功");
    }

}
