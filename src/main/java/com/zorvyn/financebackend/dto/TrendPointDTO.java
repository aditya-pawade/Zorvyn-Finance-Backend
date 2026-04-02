package com.zorvyn.financebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendPointDTO {
    private String period;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;
}