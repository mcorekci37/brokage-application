package com.emce.brokage.balance.dto;

import com.emce.brokage.entity.TransactionStatus;
import lombok.Builder;

@Builder
public record BalanceResponse(Integer customerId, Double previousAmount, Double currentAmount, TransactionStatus transactionStatus) {
}
