package com.zorvyn.financebackend.dto;

import com.zorvyn.financebackend.model.FinancialRecord;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecordDTO {

    private Long id;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private FinancialRecord.Type type;

    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "User ID is required")
    private Long userId;
}