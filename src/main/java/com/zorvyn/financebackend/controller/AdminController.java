package com.zorvyn.financebackend.controller;

import com.zorvyn.financebackend.dto.UserDTO;
import com.zorvyn.financebackend.model.FinancialRecord;
import com.zorvyn.financebackend.model.User;
import com.zorvyn.financebackend.service.FinancialRecordService;
import com.zorvyn.financebackend.service.UserService;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final FinancialRecordService recordService;

    public AdminController(UserService userService, FinancialRecordService recordService) {
        this.userService = userService;
        this.recordService = recordService;
    }

    

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = User.builder()
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .role(userDTO.getRole())
                .status(User.Status.ACTIVE)
                .build();
        User created = userService.createUser(user);
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/records")
    public ResponseEntity<List<FinancialRecord>> getAllRecords() {
        return ResponseEntity.ok(recordService.getAllRecords());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/records/{id}")
    public ResponseEntity<FinancialRecord> getRecordById(@PathVariable Long id) {
        FinancialRecord record = recordService.getRecordById(id);
        return ResponseEntity.ok(record);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/records/user/{userId}")
    public ResponseEntity<List<FinancialRecord>> getRecordsByUser(@PathVariable Long userId) {
        List<FinancialRecord> records = recordService.getRecordsByUserId(userId);
        return ResponseEntity.ok(records);
    }
}