package com.emce.brokage.auth.dto;

import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.auth.entity.Role;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(Integer id, String name, String email,
                           LocalDateTime createdAt, LocalDateTime updatedAt, Role role) {
    public static UserResponse fromEntity(Customer customer) {
        return UserResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .role(customer.getRole())
                .build();
    }
}
