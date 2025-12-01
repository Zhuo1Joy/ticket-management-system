package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeService {

    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    EmailService emailService;

    private static final String CODE_PREFIX = "verification_code:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final long CODE_EXPIRE_MINUTES = 5; // 5分钟过期
    private static final long RATE_LIMIT_SECONDS = 60; // 60秒内只能发送一次

    // 发送邮箱验证码
    public void sendEmailCode(String email) {

//        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
//        if (email.matches(emailRegex)) throw new BusinessException(400, "无效的邮箱");

        // 检查发送频率
        String rateLimitKey = RATE_LIMIT_PREFIX + email;
        if (redisTemplate.opsForValue().get(rateLimitKey) != null) {
            throw new BusinessException(400, "发送过于频繁，请稍后重试");
        }

        // 生成验证码
        String code = emailService.generateVerificationCode();

        // 存储到Redis（5分钟过期）
        String key = CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 发送频率限制
        // PS:这个值不是很重要 主要看Redis里面有没有这个键
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        // 发送邮件
        emailService.sendVerificationCode(email, code);
    }

    // 验证邮箱验证码
    public boolean verifyEmailCode(String email, String code) {

        String key = CODE_PREFIX + email;

        // 验证码不存在或已经过期
        if (redisTemplate.opsForValue().get(key) != null) {
            // 验证码存在
            if (code.equals(redisTemplate.opsForValue().get(key))) {
                // 删除验证码
                redisTemplate.delete(key);
                return true;
            }
        }

        return false;

    }

}
