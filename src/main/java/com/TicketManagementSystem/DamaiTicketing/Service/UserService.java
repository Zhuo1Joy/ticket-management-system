package com.TicketManagementSystem.DamaiTicketing.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.User;
import com.TicketManagementSystem.DamaiTicketing.Entity.UserDTO;
import com.TicketManagementSystem.DamaiTicketing.Manager.UserManager;
import com.TicketManagementSystem.DamaiTicketing.Mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    UserManager userManager;

    // 太不优雅了
    UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(userDTO.getEmail());
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
        return toDTO(baseMapper.selectById(StpUtil.getLoginIdAsInt()));
    }

    // 修改个人信息（除密码以外部分）
    public UserDTO updateUserInformation(UserDTO user) {
        return userManager.updateUserInformation(user);
    }

}
