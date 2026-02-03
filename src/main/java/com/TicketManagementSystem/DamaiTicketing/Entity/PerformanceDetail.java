package com.TicketManagementSystem.DamaiTicketing.Entity;

import lombok.Data;

import java.util.List;

@Data
public class PerformanceDetail {

    PerformanceSession performanceSession;
    List<TicketTier> ticketTiers;

}
