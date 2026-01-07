package com.TicketManagementSystem.DamaiTicketing.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.LoginRequest;
import com.TicketManagementSystem.DamaiTicketing.Entity.User;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class LoginService extends ServiceImpl<UserMapper, User> {

    final
    VerificationCodeService verificationCodeService;

    public LoginService(VerificationCodeService verificationCodeService) {
        this.verificationCodeService = verificationCodeService;
    }

    // 登录模块->生成Token
    // 听从学姐建议将这些七七八八的代码挪到服务层来了 但是看起来似乎并没有更加优雅（思考）
    public String login(LoginRequest loginRequest) {

        User user = new User();
        switch(loginRequest.getLoginType()) {
            case "password":
                user = loginByPassword(loginRequest.getUsername(), loginRequest.getPassword());
                break;
            case "email":
                user = loginByEmail(loginRequest.getEmail(), loginRequest.getCode());
                break;
            case "WeChat":
                break;
            default:
                // 忘记设置这个导致乱登录hh
                throw new BusinessException(400, "请选择登录方式");
        }

        if (user != null) {
            StpUtil.login(user.getId());    // 芥末帅！
            // 在这里最开始存进去的是getUsername() 不过后来想想不太对 我都允许修改username了 那万一哪天出了奇怪的BUG查到别人的信息不就完蛋了
            // 同理 登录这一块我也要好好想想
            return StpUtil.getTokenValue();
        }
        return null;
    }

    // 密码登录
    public User loginByPassword(String username, String password) {

        User user = lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        // 用户名或密码为空的情况
        // 其实这个优雅的写法是AI给的↓ 这也太优雅了 是我肯定 == null 了
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            // 这里本来还在想怎么返回错误响应 然后发现AI为我提供的做法居然是直接抛出异常
            // throw new BusinessException("用户名和密码不能为空");
            // 但是我又不是很想这样干 所以我试着返回错误信息 在Controller层返回错误响应
            // return "用户名和密码不能为空";
            // 很快我就发现这里有问题 我只返回错误信息的话 我怎么判断这个信息应该是Response.success()还是Response.error()呢 所以计划失败
            throw new BusinessException(401, "用户名和密码不能为空");
            // 现在我理解了 我这个异常抛出去会在Controller层被全局异常处理器抓到 然后返回错误响应 简直就是天才

        }
        // 用户不存在
        // 因为是密码登录所以没有注册新用户的功能
        if (user == null) {
            throw new BusinessException(401, "该用户不存在");
        }
        // 密码错误的情况
        if (!password.equals(user.getPassword())) {
            throw new BusinessException(401, "密码错误");
        }

        // 没有问题的情况
        return user;

    }

    // 发送邮箱验证码
    public void sendEmailCode(String email) {
        verificationCodeService.sendEmailCode(email);
    }

    // 邮箱登录
    public User loginByEmail(String email, String code) {

        User user = lambdaQuery()
                .eq(User::getEmail, email)
                .one();

        // 同样先看用户有没有输入邮箱和验证码
        if (StringUtils.isBlank(email) || StringUtils.isBlank(code)) {
            throw new BusinessException(401, "邮箱和验证码不能为空");
        }

        // 然后看验证码对不对
        if (!verificationCodeService.verifyEmailCode(email, code)) throw new BusinessException(401, "验证码错误");

        // 再看数据库里有没有这个邮箱
        if (user != null) {
            return user; // 没问题 返回用户~
        }

        // 没有 创建新用户
        user = new User();
        user.setUsername(email); // 默认用户名为邮箱
        user.setPassword("123456"); // 默认密码为123456
        user.setEmail(email);
        Random random = new Random();
        user.setNickname("DaMai"+ random.nextInt(1000000)); // 默认昵称随机生成 本来想生成字符串的 发现有点麻烦 于是生成数字hh

        if (!save(user)) throw new BusinessException(401, "用户创建失败、该邮箱已被注册");// 这里感觉加个异常处理比较好（思考）毕竟我设定了username是唯一的 也能避免一个邮箱被多人注册吧？
        return user;

    }

    // 退出登录
    public boolean logout() {
        StpUtil.logout(); // Sa-Token你太帅了
        return true;
    }

}
