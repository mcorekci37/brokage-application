package com.emce.brokage.balance.dto;

import com.emce.brokage.balance.entity.TransactionStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BalanceResponse(Integer customerId, BigDecimal previousAmount, BigDecimal currentAmount, TransactionStatus transactionStatus) {
}
