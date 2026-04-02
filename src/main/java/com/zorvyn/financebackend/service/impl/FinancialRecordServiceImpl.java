package com.zorvyn.financebackend.service.impl;

import com.zorvyn.financebackend.exception.BadRequestException;
import com.zorvyn.financebackend.exception.ResourceNotFoundException;
import com.zorvyn.financebackend.model.FinancialRecord;
import com.zorvyn.financebackend.model.User;
import com.zorvyn.financebackend.repository.FinancialRecordRepository;
import com.zorvyn.financebackend.repository.UserRepository;
import com.zorvyn.financebackend.service.FinancialRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository recordRepo;
    private final UserRepository userRepo;

    public FinancialRecordServiceImpl(FinancialRecordRepository recordRepo, UserRepository userRepo) {
        this.recordRepo = recordRepo;
        this.userRepo = userRepo;
    }

    @Override
    public FinancialRecord createRecord(FinancialRecord record) {
        if (record.getUser() == null || record.getUser().getId() == null) {
            throw new BadRequestException("User ID must be provided for creating financial record");
        }
        
        Long userId = record.getUser().getId();
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        record.setUser(user);
        return recordRepo.save(record);
    }

    @Override
    public List<FinancialRecord> getAllRecords() {
        return recordRepo.findAll();
    }

    @Override
    public FinancialRecord getRecordById(Long id) {
        return recordRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id " + id));
    }

    @Override
    public List<FinancialRecord> getRecordsByUserId(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        return recordRepo.findByUser(user);
    }

    @Override
    public List<FinancialRecord> filterRecords(LocalDate date, LocalDate startDate, LocalDate endDate, String category, FinancialRecord.Type type) {
        if (date != null && (startDate != null || endDate != null)) {
            throw new BadRequestException("Use either 'date' or 'startDate/endDate' for filtering, not both");
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate cannot be after endDate");
        }

        String normalizedCategory = category == null ? null : category.trim();

        return recordRepo.findAll().stream()
                .filter(record -> date == null || (record.getDate() != null && record.getDate().equals(date)))
                .filter(record -> startDate == null || (record.getDate() != null && !record.getDate().isBefore(startDate)))
                .filter(record -> endDate == null || (record.getDate() != null && !record.getDate().isAfter(endDate)))
                .filter(record -> normalizedCategory == null || normalizedCategory.isEmpty()
                        || (record.getCategory() != null && record.getCategory().equalsIgnoreCase(normalizedCategory)))
                .filter(record -> type == null || record.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public FinancialRecord updateRecord(Long id, FinancialRecord record) {
        FinancialRecord existingRecord = recordRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id " + id));

        if (record.getUser() == null || record.getUser().getId() == null) {
            throw new BadRequestException("User ID must be provided for updating financial record");
        }

        Long userId = record.getUser().getId();
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        existingRecord.setAmount(record.getAmount());
        existingRecord.setType(record.getType());
        existingRecord.setCategory(record.getCategory());
        existingRecord.setDescription(record.getDescription());
        existingRecord.setDate(record.getDate());
        existingRecord.setUser(user);

        return recordRepo.save(existingRecord);
    }

    @Override
    public void deleteRecord(Long id) {
        if (!recordRepo.existsById(id)) {
            throw new ResourceNotFoundException("Record not found with id " + id);
        }
        recordRepo.deleteById(id);
    }
}