package com.TicketManagementSystem.DamaiTicketing.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class EmailService {

    // 注入Spring Boot自动配置的邮件发送器
    @Autowired
    private JavaMailSender mailSender;

    // 从配置文件中读取发件人邮箱地址
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String verificationCode) {
        try {
            // 创建MIME邮件消息 支持HTML内容和附件
            MimeMessage message = mailSender.createMimeMessage();
            // 使用MimeMessageHelper简化邮件设置 true表示支持多部分消息
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 设置发件人邮箱（从配置文件读取）
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("您的登录验证码");

            // HTML格式的邮件内容
            String content = String.format(
                    "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">" +
                            "<h2 style=\"color: #1890ff;\">登录验证码</h2>" +
                            "<p>您好！</p>" +
                            "<p>您请求的登录验证码是：</p>" +
                            "<div style=\"text-align: center; margin: 20px 0;\">" +
                            "<span style=\"font-size: 32px; font-weight: bold; color: #1890ff; letter-spacing: 5px;\">%s</span>" +
                            "</div>" +
                            "<p>此验证码<strong>10分钟</strong>内有效！请及时使用。</p>" +
                            "<p>如非本人操作请忽略此邮件。</p>" +
                            "</div>",
                    verificationCode
            );

            helper.setText(content, true); // true表示发送HTML格式邮件
            mailSender.send(message);
            System.out.println("验证码邮件发送成功至: " + toEmail);

        } catch (Exception e) {
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    // 生成六位验证码
    public String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

}
