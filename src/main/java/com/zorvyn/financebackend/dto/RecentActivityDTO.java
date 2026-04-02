package com.zorvyn.financebackend.dto;

import com.zorvyn.financebackend.model.FinancialRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivityDTO {
    private Long recordId;
    private BigDecimal amount;
    private FinancialRecord.Type type;
    private String category;
    private String description;
    private LocalDate date;
    private Long userId;
    private String userEmail;
}