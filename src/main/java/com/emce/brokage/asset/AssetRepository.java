package com.emce.brokage.asset;

import com.emce.brokage.asset.entity.Asset;
import com.emce.brokage.asset.entity.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {
    Asset findByAssetName(AssetType assetName);
    @Query("SELECT a FROM Asset a WHERE a.customer.id = :customerId AND (:assetType IS NULL OR a.assetName = :assetType)")
    Page<Asset> findByCustomerIdAndAssetType(@Param("customerId") Integer customerId,
                                             @Param("assetType") AssetType assetType,
                                             Pageable pageable);
}
