package com.zorvyn.financebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthTokenResponseDTO {
    private String token;
    private String tokenType;
    private long expiresInMs;
    private String username;
    private List<String> authorities;
}