package com.TicketManagementSystem.DamaiTicketing.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.User;
import com.TicketManagementSystem.DamaiTicketing.Entity.UserDTO;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

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
                // 这里性别还没有做处理 没有严格设定为三个选项之一
                .set(user.getBirthday() != null, User::getBirthday, user.getBirthday())
                .update();
        if (!result) throw new BusinessException(401, "修改失败");
    }

    // TODO 修改密码（需要额外验证方式）

    // 获取用户邮箱（邮箱设置了非空所以不需要担心～）
    public String getUserEmail(Long id) {
        return lambdaQuery().eq(User::getId, id)
                .one()
                .getEmail();
    }

}
