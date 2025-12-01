package com.TicketManagementSystem.DamaiTicketing.Entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GrabTicketRequest {

    private Long performanceId;

    @NotNull(message = "场次ID不能为空")
    private Long sessionId;

    @NotNull(message = "票档ID不能为空")
    private Long tierId;

    @Min(value = 1, message = "购买数量至少1张")
    @Max(value = 2, message = "单次最多购买2张")
    private Integer quantity = 1;


}
