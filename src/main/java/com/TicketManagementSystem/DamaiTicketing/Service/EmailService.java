package com.TicketManagementSystem.DamaiTicketing.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class EmailService {

    public void sendVerificationCode(String email, String code) {
        // TODO: 这里接入真实的邮件服务（如阿里云邮件、腾讯云邮件等）

        log.info("发送邮件验证码到: {}, 验证码: {}", email, code);
        log.info("邮件内容: 您的验证码是 {}，有效期5分钟", code);

    }

    // 生成六位验证码
    public String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

}
