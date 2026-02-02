package com.TicketManagementSystem.DamaiTicketing.Entity;

import lombok.Data;

@Data
public class PerformanceRequest {

    private Performance performance;
    private PerformanceSession performanceSession;
    private TicketTier ticketTier;

}
