package com.TicketManagementSystem.DamaiTicketing.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.User;
import com.TicketManagementSystem.DamaiTicketing.Entity.UserDTO;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    // TODO 支持PC、APP等不同设备设置不同Token过期策略；管理端与用户端不同的过期策略

    final
    VerificationCodeService verificationCodeService;

    public UserService(VerificationCodeService verificationCodeService) {
        this.verificationCodeService = verificationCodeService;
    }

    // 不优雅之
    UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setNickname(user.getNickname());
        userDTO.setGender(user.getGender());
        userDTO.setBirthday(user.getBirthday());
        userDTO.setCreateTime(user.getCreateTime());
        userDTO.setLastLoginTime(LocalDateTime.now());
        return userDTO;
    }

    // 获取当前用户信息
    public UserDTO getUserInformation() {
        // return toDTO(baseMapper.selectById(StpUtil.getLoginIdAsLong()));
        return toDTO(this.lambdaQuery()
                .eq(User::getId, StpUtil.getLoginIdAsLong())
                .one());
    }

    // 修改个人信息（除密码以外部分）
    public void updateUserInformation(UserDTO user) {
        boolean result = this.lambdaUpdate()
                .eq(User::getId, StpUtil.getLoginIdAsLong())
                .set(user.getUsername() != null, User::getUsername, user.getUsername())
                .set(user.getEmail() != null, User::getEmail, user.getEmail())
                .set(user.getPhone() != null, User::getPhone, user.getPhone())
                .set(user.getNickname() != null, User::getNickname, user.getNickname())
                .set(user.getGender() != null, User::getGender, user.getGender())
                .set(user.getBirthday() != null, User::getBirthday, user.getBirthday())
                .update();
        if (!result) throw new BusinessException(401, "修改失败");
    }

    // 获取用户邮箱（邮箱设置了非空所以不需要担心～）
    public String getUserEmail(Long id) {
        return lambdaQuery().eq(User::getId, id)
                .one()
                .getEmail();
    }

    // 修改密码
    // 在这里发送验证码的方法就用之前写过的那个登录验证码（毕竟改的话只是改改邮件内容 意义不大
    public void updateUserPassword(String code, String password) {

         User user = lambdaQuery()
                 .eq(User::getId, StpUtil.getLoginIdAsLong())
                 .one();

         String email = user.getEmail();

        // 先看用户有没有输入验证码
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(401, "验证码不能为空");
        }

        // 然后看验证码对不对
        if (!verificationCodeService.verifyEmailCode(email, code)) throw new BusinessException(401, "验证码错误");

        // 检查传入的密码是否为空
        // TODO 密码加密
        if (password.isEmpty()) throw new BusinessException(401, "新的密码不能为空");

        // 检查密码是否与原来的密码一致
        if (password.equals(user.getPassword())) throw new BusinessException(401, "新密码不能与原来的密码一样");

        // 检查密码安全性
        boolean hasMinLength = password.length() >= 8;
        boolean hasUpperCase = !password.equals(password.toLowerCase());
        boolean hasLowerCase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");

        if (!(hasMinLength || hasUpperCase || hasLowerCase || hasDigit || hasSpecialChar)) {
            throw new BusinessException(401, "密码安全性不足");
        }

        // 没有问题 允许修改密码
        boolean result = this.lambdaUpdate()
                .eq(User::getId, StpUtil.getLoginIdAsLong())
                .set(User::getPassword, password)
                .update();

        if (!result) {
            log.error("修改密码失败");
            throw new BusinessException(400, "修改密码失败");
        }

        StpUtil.kickout(StpUtil.getLoginId());

    }

}
