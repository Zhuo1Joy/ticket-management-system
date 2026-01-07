package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.TicketManagementSystem.DamaiTicketing.Enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING) // 指定存储为字符串
    @Column(name = "status", length = 20, nullable = false) // 指定长度和非空
    private Gender gender;

    private Date birthday;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;

}
