package com.emce.brokage.order.dto;

import com.emce.brokage.asset.entity.AssetType;
import com.emce.brokage.order.entity.Order;
import com.emce.brokage.order.entity.OrderSide;
import com.emce.brokage.order.entity.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record OrderResponse(Integer customerId, AssetType assetName, OrderSide side, BigDecimal size, BigDecimal price,
                            OrderStatus status, LocalDateTime createDate, LocalDateTime updateDate) {
    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .customerId(order.getCustomer().getId())
                .assetName(order.getAssetName())
                .side(order.getOrderSide())
                .size(order.getSize())
                .price(order.getPrice())
                .status(order.getStatus())
                .createDate(order.getCreatedAt())
                .updateDate(order.getUpdatedAt())
                .build();
    }
}
