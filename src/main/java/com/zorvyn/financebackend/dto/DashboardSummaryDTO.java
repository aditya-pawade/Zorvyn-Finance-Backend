package com.zorvyn.financebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private List<CategoryTotalDTO> categoryTotals;
    private List<RecentActivityDTO> recentActivity;
    private List<TrendPointDTO> monthlyTrends;
    private List<TrendPointDTO> weeklyTrends;
}