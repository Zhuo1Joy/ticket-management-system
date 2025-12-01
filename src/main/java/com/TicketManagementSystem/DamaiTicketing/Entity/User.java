package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

// 告诉MyBatis-Plus这是user表
@TableName("user")
@Data
@NoArgsConstructor // 还必须要有无参构造方法
public class User {

    // 本来想在User表里面直接加上identity字段来判断用户or管理员身份 但是管理员要做的操作应该会很多 感觉还是得新建个表
    @TableId  // 告诉MyBatis-Plus这是主键
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String nickname;
    private String gender;
    private Date birthday;
    private int status;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;

}
