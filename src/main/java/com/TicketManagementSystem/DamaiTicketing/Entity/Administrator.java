package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("administrator")
@Data
public class Administrator {

    @TableId
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
