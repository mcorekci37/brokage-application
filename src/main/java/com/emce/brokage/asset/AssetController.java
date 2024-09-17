package com.emce.brokage.asset;

import com.emce.brokage.asset.dto.AssetDto;
import com.emce.brokage.asset.entity.AssetType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/asset")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/list/{customerId}")
    public ResponseEntity<Page<AssetDto>> getAssetsForCustomer(
            @PathVariable Integer customerId,
            @RequestParam(required = false) AssetType assetName, // Optional asset type filter
            Pageable pageable) {
        Page<AssetDto> assets = assetService.getAssetsForCustomer(customerId, assetName, pageable);
        return ResponseEntity.ok(assets);
    }

}
