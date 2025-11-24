package com.TicketManagementSystem.DamaiTicketing.Entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "登录类型不能为空")
    private String loginType; // password, email, WeChat

    // 密码登录参数
    private String username;
    private String password;

    // 邮箱登录参数
    private String email;
    private String code; // 邮箱验证码

    // 微信登录参数
    private String wechatCode;

}
