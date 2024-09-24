package com.emce.brokage.balance.dto;

import com.emce.brokage.balance.entity.TransactionType;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BalanceRequest(
        @Positive(message = "customerId must be positive")
        Integer customerId,
        @Positive(message = "amount must be positive")
        BigDecimal amount,
        TransactionType transactionType) {
}
