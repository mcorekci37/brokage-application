package com.emce.brokage.balance.dto;

import com.emce.brokage.entity.TransactionType;
import lombok.Builder;

@Builder
public record BalanceRequest(Integer customerId, Double amount, TransactionType transactionType) {
}
