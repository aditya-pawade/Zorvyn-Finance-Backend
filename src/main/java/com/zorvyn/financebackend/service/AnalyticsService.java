package com.zorvyn.financebackend.service;

import com.zorvyn.financebackend.dto.AnalystInsightsDTO;
import com.zorvyn.financebackend.dto.CategoryTotalDTO;
import com.zorvyn.financebackend.dto.DashboardSummaryDTO;
import com.zorvyn.financebackend.dto.RecentActivityDTO;
import com.zorvyn.financebackend.dto.TrendPointDTO;
import com.zorvyn.financebackend.model.FinancialRecord;

import java.math.BigDecimal;
import java.util.List;

public interface AnalyticsService {
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpense();
    BigDecimal getNetBalance();
    List<CategoryTotalDTO> getCategoryTotals();
    List<RecentActivityDTO> getRecentActivity(int limit);
    List<TrendPointDTO> getMonthlyTrends(int months);
    List<TrendPointDTO> getWeeklyTrends(int weeks);
    DashboardSummaryDTO getDashboardSummary(int recentLimit, int monthPoints, int weekPoints);
    AnalystInsightsDTO getAnalystInsights(int categoryLimit, int largestExpensesLimit);
    List<CategoryTotalDTO> getTopCategories(FinancialRecord.Type type, int limit);
}