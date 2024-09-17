package com.emce.brokage.asset;

import com.emce.brokage.asset.dto.AssetDto;
import com.emce.brokage.asset.entity.AssetType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    @PreAuthorize("#customerId == authentication.principal.id")
    public Page<AssetDto> getAssetsForCustomer(Integer customerId, AssetType assetName, Pageable pageable) {
        return assetRepository.findByCustomerIdAndAssetType(customerId, assetName, pageable)
                .map(asset -> AssetDto.fromEntity(asset));
    }
}
