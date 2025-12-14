package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Entity.User;
import com.TicketManagementSystem.DamaiTicketing.Entity.UserDTO;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.UserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    UserService userService;

    // 分页查询用户信息
    // 这里查询可以支持根据所有信息查询 根据需求随时可改
    public Page<String> getUser(int pageNum) {

        int currentPage = Math.max(pageNum, 1);
        Page<User> page = new Page<>(currentPage, 20);

        Page<User> users =  this.lambdaQuery().page(page);

        List<String> result = users.getRecords()
                .stream()
                .map(User::getUsername)
                .toList();
//
//        Page<UserDTO> resultPage = new Page<>();
//        BeanUtils.copyProperties(result, resultPage);  // 这个也可以复制分页信息
//
//        List<User> users = result.getRecords();
//        resultPage.setRecords(userService.toDTO(users));

        Page<String> pageUser = new Page<>(currentPage, 20);
        pageUser.setTotal(users.getTotal());
        pageUser.setRecords(result);

        if (currentPage > pageUser.getPages()) throw new BusinessException(404, "暂无可查询用户");
        return pageUser;

    }

    // 查询用户详情
    public UserDTO getUserDetails(Long userId) {
        User result = this.lambdaQuery()
                .eq(User::getId, userId)
                .one();

        if (result != null) return userService.toDTO(result);
        else throw new BusinessException(404, "您查询的用户不存在");
    }

    // 修改用户信息（重置用户密码）
    // 如果需要改别的也行 都是小问题 只是我觉得管理员随便改用户信息不太好
    public void updateUserInformation(Long userId) {
        boolean result = this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getPassword, 123456)
                .update();

        if (!result) throw new RuntimeException("修改用户信息失败");
    }

    // 启用/禁用用户
    public void updateUserStatus(Long userId, int status) {
        boolean result = this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getStatus, status)
                .update();

        if (!result) throw new RuntimeException("修改用户状态失败");
    }

}
