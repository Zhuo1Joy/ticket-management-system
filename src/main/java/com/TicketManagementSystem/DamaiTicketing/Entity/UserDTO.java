package com.TicketManagementSystem.DamaiTicketing.Entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String gender;
    private Date birthday;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;

}
