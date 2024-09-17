package com.emce.brokage.asset.dto;

import com.emce.brokage.asset.entity.Asset;
import com.emce.brokage.asset.entity.AssetType;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AssetDto(Integer id, AssetType assetName, BigDecimal size, BigDecimal usableSize, Integer customerId) {
    public static AssetDto fromEntity(Asset asset){
        return AssetDto.builder()
                .id(asset.getId())
                .assetName(asset.getAssetName())
                .size(asset.getSize())
                .usableSize(asset.getUsableSize())
                .customerId(asset.getCustomer().getId())
                .build();
    }
}
