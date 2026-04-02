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
public class AnalystInsightsDTO {
    private BigDecimal averageIncomeTransaction;
    private BigDecimal averageExpenseTransaction;
    private BigDecimal savingsRatePercent;
    private String highestExpenseCategory;
    private BigDecimal highestExpenseCategoryTotal;
    private long incomeTransactionCount;
    private long expenseTransactionCount;
    private List<CategoryTotalDTO> topExpenseCategories;
    private List<RecentActivityDTO> largestExpenses;
}