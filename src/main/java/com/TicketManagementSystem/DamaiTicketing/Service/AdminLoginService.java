package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.Administrator;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.AdminMapper;
import com.TicketManagementSystem.DamaiTicketing.Util.StpAdminUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class AdminLoginService extends ServiceImpl<AdminMapper, Administrator> {

    public String loginByPassword(String username, String password) {

        Administrator admin = lambdaQuery()
                .eq(Administrator::getUsername, username)
                .one();

        // 用户名或密码为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BusinessException(401, "用户名和密码不能为空");
        }
        // 用户不存在
        if (admin == null) {
            throw new BusinessException(401, "该管理员账号不存在");
        }
        // 密码错误
        if (!password.equals(admin.getPassword())) {
            throw new BusinessException(401, "密码错误");
        }

        // 没有问题的情况 返回Token
        StpAdminUtil.login(admin.getId());
        return StpAdminUtil.getTokenValue();

    }

    // 退出登录
    public boolean logout() {
        StpAdminUtil.logout();
        return true;
    }

}
