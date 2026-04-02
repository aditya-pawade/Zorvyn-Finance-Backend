package com.zorvyn.financebackend.service.impl;

import com.zorvyn.financebackend.dto.AnalystInsightsDTO;
import com.zorvyn.financebackend.dto.CategoryTotalDTO;
import com.zorvyn.financebackend.dto.DashboardSummaryDTO;
import com.zorvyn.financebackend.dto.RecentActivityDTO;
import com.zorvyn.financebackend.dto.TrendPointDTO;
import com.zorvyn.financebackend.model.FinancialRecord;
import com.zorvyn.financebackend.repository.FinancialRecordRepository;
import com.zorvyn.financebackend.service.AnalyticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final FinancialRecordRepository financialRecordRepository;

    public AnalyticsServiceImpl(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    @Override
    public BigDecimal getTotalIncome() {
        return sumByType(financialRecordRepository.findAll(), FinancialRecord.Type.INCOME);
    }

    @Override
    public BigDecimal getTotalExpense() {
        return sumByType(financialRecordRepository.findAll(), FinancialRecord.Type.EXPENSE);
    }

    @Override
    public BigDecimal getNetBalance() {
        BigDecimal income = getTotalIncome();
        BigDecimal expense = getTotalExpense();
        return income.subtract(expense);
    }

    @Override
    public List<CategoryTotalDTO> getCategoryTotals() {
        return buildCategoryTotals(financialRecordRepository.findAll(), null, Integer.MAX_VALUE);
    }

    @Override
    public List<RecentActivityDTO> getRecentActivity(int limit) {
        int safeLimit = Math.max(1, limit);
        return financialRecordRepository.findAll().stream()
                .sorted(Comparator
                        .comparing(FinancialRecord::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FinancialRecord::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(safeLimit)
                .map(this::toRecentActivity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrendPointDTO> getMonthlyTrends(int months) {
        int safeMonths = Math.max(1, months);
        List<FinancialRecord> records = financialRecordRepository.findAll();
        List<TrendPointDTO> points = new ArrayList<>();

        YearMonth current = YearMonth.now();
        for (int i = safeMonths - 1; i >= 0; i--) {
            YearMonth period = current.minusMonths(i);
            LocalDate from = period.atDay(1);
            LocalDate to = period.atEndOfMonth();
            points.add(buildTrendPoint(records, period.toString(), from, to));
        }

        return points;
    }

    @Override
    public List<TrendPointDTO> getWeeklyTrends(int weeks) {
        int safeWeeks = Math.max(1, weeks);
        List<FinancialRecord> records = financialRecordRepository.findAll();
        List<TrendPointDTO> points = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = safeWeeks - 1; i >= 0; i--) {
            LocalDate start = today.minusWeeks(i).with(DayOfWeek.MONDAY);
            LocalDate end = start.plusDays(6);
            String period = start + " to " + end;
            points.add(buildTrendPoint(records, period, start, end));
        }

        return points;
    }

    @Override
    public DashboardSummaryDTO getDashboardSummary(int recentLimit, int monthPoints, int weekPoints) {
        return DashboardSummaryDTO.builder()
                .totalIncome(getTotalIncome())
                .totalExpense(getTotalExpense())
                .netBalance(getNetBalance())
                .categoryTotals(getCategoryTotals())
                .recentActivity(getRecentActivity(recentLimit))
                .monthlyTrends(getMonthlyTrends(monthPoints))
                .weeklyTrends(getWeeklyTrends(weekPoints))
                .build();
    }

    @Override
    public AnalystInsightsDTO getAnalystInsights(int categoryLimit, int largestExpensesLimit) {
        List<FinancialRecord> records = financialRecordRepository.findAll();

        List<FinancialRecord> incomeRecords = records.stream()
                .filter(record -> record.getType() == FinancialRecord.Type.INCOME)
                .collect(Collectors.toList());

        List<FinancialRecord> expenseRecords = records.stream()
                .filter(record -> record.getType() == FinancialRecord.Type.EXPENSE)
                .collect(Collectors.toList());

        BigDecimal totalIncome = sumByType(records, FinancialRecord.Type.INCOME);
        BigDecimal totalExpense = sumByType(records, FinancialRecord.Type.EXPENSE);
        BigDecimal savingsRate = totalIncome.compareTo(ZERO) == 0
                ? ZERO
                : totalIncome.subtract(totalExpense)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalIncome, 2, RoundingMode.HALF_UP);

        BigDecimal avgIncome = averageAmount(incomeRecords);
        BigDecimal avgExpense = averageAmount(expenseRecords);

        List<CategoryTotalDTO> topExpenseCategories = buildCategoryTotals(records, FinancialRecord.Type.EXPENSE,
                Math.max(1, categoryLimit));

        CategoryTotalDTO highestExpenseCategory = topExpenseCategories.isEmpty()
                ? CategoryTotalDTO.builder().category("N/A").total(ZERO).build()
                : topExpenseCategories.get(0);

        List<RecentActivityDTO> largestExpenses = expenseRecords.stream()
                .filter(record -> record.getAmount() != null)
                .sorted(Comparator.comparing(FinancialRecord::getAmount, Comparator.reverseOrder()))
                .limit(Math.max(1, largestExpensesLimit))
                .map(this::toRecentActivity)
                .collect(Collectors.toList());

        return AnalystInsightsDTO.builder()
                .averageIncomeTransaction(avgIncome)
                .averageExpenseTransaction(avgExpense)
                .savingsRatePercent(savingsRate)
                .highestExpenseCategory(highestExpenseCategory.getCategory())
                .highestExpenseCategoryTotal(highestExpenseCategory.getTotal())
                .incomeTransactionCount(incomeRecords.size())
                .expenseTransactionCount(expenseRecords.size())
                .topExpenseCategories(topExpenseCategories)
                .largestExpenses(largestExpenses)
                .build();
    }

    @Override
    public List<CategoryTotalDTO> getTopCategories(FinancialRecord.Type type, int limit) {
        int safeLimit = Math.max(1, limit);
        return buildCategoryTotals(financialRecordRepository.findAll(), type, safeLimit);
    }

    private TrendPointDTO buildTrendPoint(List<FinancialRecord> records, String period, LocalDate from, LocalDate to) {
        List<FinancialRecord> scoped = records.stream()
                .filter(record -> record.getDate() != null)
                .filter(record -> !record.getDate().isBefore(from) && !record.getDate().isAfter(to))
                .collect(Collectors.toList());

        BigDecimal income = sumByType(scoped, FinancialRecord.Type.INCOME);
        BigDecimal expense = sumByType(scoped, FinancialRecord.Type.EXPENSE);

        return TrendPointDTO.builder()
                .period(period)
                .income(income)
                .expense(expense)
                .net(income.subtract(expense))
                .build();
    }

    private BigDecimal sumByType(List<FinancialRecord> records, FinancialRecord.Type type) {
        return records.stream()
                .filter(record -> record.getType() == type)
                .map(FinancialRecord::getAmount)
                .filter(amount -> amount != null)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal averageAmount(List<FinancialRecord> records) {
        List<BigDecimal> amounts = records.stream()
                .map(FinancialRecord::getAmount)
                .filter(amount -> amount != null)
                .collect(Collectors.toList());

        if (amounts.isEmpty()) {
            return ZERO;
        }

        BigDecimal total = amounts.stream().reduce(ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);
    }

    private List<CategoryTotalDTO> buildCategoryTotals(List<FinancialRecord> records, FinancialRecord.Type type,
            int limit) {
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();

        records.stream()
                .filter(record -> type == null || record.getType() == type)
                .filter(record -> record.getCategory() != null && !record.getCategory().trim().isEmpty())
                .filter(record -> record.getAmount() != null)
                .forEach(record -> {
                    String category = record.getCategory().trim();
                    categoryTotals.merge(category, record.getAmount(), BigDecimal::add);
                });

        return categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> CategoryTotalDTO.builder()
                        .category(entry.getKey())
                        .total(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private RecentActivityDTO toRecentActivity(FinancialRecord record) {
        return RecentActivityDTO.builder()
                .recordId(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .description(record.getDescription())
                .date(record.getDate())
                .userId(record.getUser() != null ? record.getUser().getId() : null)
                .userEmail(record.getUser() != null ? record.getUser().getEmail() : null)
                .build();
    }
}