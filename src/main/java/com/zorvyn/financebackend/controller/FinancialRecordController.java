package com.zorvyn.financebackend.controller;

import com.zorvyn.financebackend.dto.FinancialRecordDTO;
import com.zorvyn.financebackend.model.FinancialRecord;
import com.zorvyn.financebackend.model.User;
import com.zorvyn.financebackend.service.FinancialRecordService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    public FinancialRecordController(FinancialRecordService recordService) {
        this.recordService = recordService;
    }

    
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @PostMapping
    public ResponseEntity<FinancialRecord> createRecord(@Valid @RequestBody FinancialRecordDTO recordDTO) {
        FinancialRecord record = FinancialRecord.builder()
                .amount(recordDTO.getAmount())
                .type(recordDTO.getType())
                .category(recordDTO.getCategory())
                .description(recordDTO.getDescription())
                .date(recordDTO.getDate())
                .user(User.builder().id(recordDTO.getUserId()).build())
                .build();

        FinancialRecord created = recordService.createRecord(record);
        return ResponseEntity.ok(created);
    }

    
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping
    public ResponseEntity<List<FinancialRecord>> getAllRecords() {
        return ResponseEntity.ok(recordService.getAllRecords());
    }

    
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<FinancialRecord> getRecordById(@PathVariable Long id) {
        FinancialRecord record = recordService.getRecordById(id);
        return ResponseEntity.ok(record);
    }

    
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FinancialRecord>> getRecordsByUser(@PathVariable Long userId) {
        List<FinancialRecord> records = recordService.getRecordsByUserId(userId);
        return ResponseEntity.ok(records);
    }

    
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @GetMapping("/filter")
    public ResponseEntity<List<FinancialRecord>> filterRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) FinancialRecord.Type type) {
        return ResponseEntity.ok(recordService.filterRecords(date, startDate, endDate, category, type));
    }

    
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FinancialRecord> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordDTO recordDTO) {
        FinancialRecord record = FinancialRecord.builder()
                .amount(recordDTO.getAmount())
            .type(recordDTO.getType())
            .category(recordDTO.getCategory())
                .description(recordDTO.getDescription())
                .date(recordDTO.getDate())
                .user(User.builder().id(recordDTO.getUserId()).build())
                .build();

        FinancialRecord updated = recordService.updateRecord(id, record);
        return ResponseEntity.ok(updated);
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}