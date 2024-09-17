package com.emce.brokage.order.dto;

import com.emce.brokage.balance.entity.AssetType;
import com.emce.brokage.entity.OrderSide;

import java.math.BigDecimal;

public record OrderRequest(Integer customerId, AssetType assetName, OrderSide orderSide, BigDecimal size, BigDecimal price) {
}
