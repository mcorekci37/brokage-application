package com.emce.brokage.order.dto;

import com.emce.brokage.asset.entity.AssetType;
import com.emce.brokage.order.entity.OrderSide;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderRequest(
        @Positive(message = "customerId must be positive")
        Integer customerId,
        AssetType assetName,
        OrderSide orderSide,
        @Positive(message = "size must be positive")
        BigDecimal size,
        @Positive(message = "price must be positive")
        BigDecimal price) {
}
