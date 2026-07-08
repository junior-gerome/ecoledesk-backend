package com.school.platform.reporting.application.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectionDashboardDTO {
    private long todayAbsences;
    private long overduePayments;
    private long overdueAmount;
    private long excellentResults;
    private long weakResults;
    private long overloadedClasses;
    private long pendingPreRegistrations;
    private long recentAuditEntries;
}
