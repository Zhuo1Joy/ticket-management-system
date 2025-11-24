package com.TicketManagementSystem.DamaiTicketing.Mapper;

import com.TicketManagementSystem.DamaiTicketing.Entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User>{

    /*
    // 根据username查找用户
    @Select("select * from user where username = #{username}")
    User selectByUsername(String userId);

    // 根据邮箱查找用户
    @Select("select * from user where email = #{email}")
    User selectByEmail(String email);

    // 整半天自动获取的CRUD方法原来是拿id检索的吗hh

    // 创建新用户
    @Insert("insert into user(username, password, nickname) " +
            "values (#{username}, #{password}, #{nickname})")
    void insertUser(User user); // 返回此次操作影响的记录数

    // 获取当前用户信息
    @Select("select * from user where id = #{id}")
    UserDTO getUserInformation(int id);

    // 修改个人信息
    //UserDTO updateUserInformation(int id, UserDTO user);
    */

}
