package com.zorvyn.financebackend.controller;

import com.zorvyn.financebackend.dto.CategoryTotalDTO;
import com.zorvyn.financebackend.dto.DashboardSummaryDTO;
import com.zorvyn.financebackend.dto.RecentActivityDTO;
import com.zorvyn.financebackend.dto.TrendPointDTO;
import com.zorvyn.financebackend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AnalyticsService analyticsService;

    public DashboardController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(
            @RequestParam(defaultValue = "10") int recentLimit,
            @RequestParam(defaultValue = "6") int monthPoints,
            @RequestParam(defaultValue = "8") int weekPoints) {
        return ResponseEntity.ok(analyticsService.getDashboardSummary(recentLimit, monthPoints, weekPoints));
    }

    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping("/total-income")
    public ResponseEntity<Map<String, BigDecimal>> getTotalIncome() {
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalIncome", analyticsService.getTotalIncome());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping("/total-expense")
    public ResponseEntity<Map<String, BigDecimal>> getTotalExpense() {
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalExpense", analyticsService.getTotalExpense());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping("/net-balance")
    public ResponseEntity<Map<String, BigDecimal>> getNetBalance() {
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("netBalance", analyticsService.getNetBalance());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping("/category-totals")
    public ResponseEntity<List<CategoryTotalDTO>> getCategoryTotals() {
        return ResponseEntity.ok(analyticsService.getCategoryTotals());
    }

    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping("/recent-activity")
    public ResponseEntity<List<RecentActivityDTO>> getRecentActivity(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getRecentActivity(limit));
    }

    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping("/trends/monthly")
    public ResponseEntity<List<TrendPointDTO>> getMonthlyTrends(@RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getMonthlyTrends(months));
    }

    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping("/trends/weekly")
    public ResponseEntity<List<TrendPointDTO>> getWeeklyTrends(@RequestParam(defaultValue = "8") int weeks) {
        return ResponseEntity.ok(analyticsService.getWeeklyTrends(weeks));
    }
}