package com.TicketManagementSystem.DamaiTicketing.Entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("address")
@Data
@NoArgsConstructor
public class Address {

    @TableId
    private Long id;
    private Long userId;
    private String recipientName;
    private String recipientPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String isDefaultAddress;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
