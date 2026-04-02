package com.zorvyn.financebackend.service;

import com.zorvyn.financebackend.model.FinancialRecord;

import java.time.LocalDate;
import java.util.List;

public interface FinancialRecordService {
    FinancialRecord createRecord(FinancialRecord record);
    List<FinancialRecord> getAllRecords();
    FinancialRecord getRecordById(Long id);
    List<FinancialRecord> getRecordsByUserId(Long userId);
    List<FinancialRecord> filterRecords(LocalDate date, LocalDate startDate, LocalDate endDate, String category, FinancialRecord.Type type);
    FinancialRecord updateRecord(Long id, FinancialRecord record);
    void deleteRecord(Long id);
}