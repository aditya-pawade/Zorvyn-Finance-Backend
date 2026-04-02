package com.zorvyn.financebackend.controller;

import com.zorvyn.financebackend.dto.AnalystInsightsDTO;
import com.zorvyn.financebackend.dto.CategoryTotalDTO;
import com.zorvyn.financebackend.model.FinancialRecord;
import com.zorvyn.financebackend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analyst")
@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
public class AnalystController {

    private final AnalyticsService analyticsService;

    public AnalystController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/insights")
    public ResponseEntity<AnalystInsightsDTO> getInsights(
            @RequestParam(defaultValue = "5") int categoryLimit,
            @RequestParam(defaultValue = "5") int largestExpensesLimit) {
        return ResponseEntity.ok(analyticsService.getAnalystInsights(categoryLimit, largestExpensesLimit));
    }

    @GetMapping("/insights/top-categories")
    public ResponseEntity<List<CategoryTotalDTO>> getTopCategories(
            @RequestParam(defaultValue = "EXPENSE") FinancialRecord.Type type,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(analyticsService.getTopCategories(type, limit));
    }
}