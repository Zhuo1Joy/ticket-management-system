package com.TicketManagementSystem.DamaiTicketing.Manager;

import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.User;
import com.TicketManagementSystem.DamaiTicketing.Entity.UserDTO;
import com.TicketManagementSystem.DamaiTicketing.Mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserManager extends ServiceImpl<UserMapper, User> {

    @Autowired
    UserMapper userMapper;

    // 根据username查找用户
    public User selectByUsername(String username) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getUsername, username);
        return getOne(wrapper);
    }

    // 根据邮箱查找用户
    public User selectByEmail(String email) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getEmail, email);
        return getOne(wrapper);
    }

    // 修改个人信息（除密码以外部分）
    @Transactional
    public UserDTO updateUserInformation(UserDTO user) {

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<User>()
                // WHERE 条件：根据ID更新
                .eq(User::getId, StpUtil.getLoginIdAsInt())
                .set(user.getUsername() != null, User::getUsername, user.getUsername())
                .set(user.getEmail() != null, User::getEmail, user.getEmail())
                .set(user.getPhone() != null, User::getPhone, user.getPhone())
                .set(user.getNickname() != null, User::getNickname, user.getNickname())
                .set(user.getGender() != null, User::getGender, user.getGender())
                // 这里性别还没有做处理 没有严格设定为三个选项之一
                .set(user.getBirthday() != null, User::getBirthday, user.getBirthday());
                // 这里同理 然后原本附上的正则表达式并不严谨:"^\\d{4}-\\d{2}-\\d{2}$" 这只规定了格式 没有严格限定月份天数闰年什么的 不过等后面再说吧（
        int result = userMapper.update(null, updateWrapper);
        System.out.println("更新成功:"+result);
        return user;

    }

}
