package com.zorvyn.financebackend.repository;

import com.zorvyn.financebackend.model.FinancialRecord;
import com.zorvyn.financebackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {
    List<FinancialRecord> findByUser(User user);
}