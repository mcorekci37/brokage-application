package com.emce.brokage.balance.dto;

import com.emce.brokage.balance.entity.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BalanceRequest(Integer customerId, BigDecimal amount, TransactionType transactionType) {
}
