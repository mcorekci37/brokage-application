package com.emce.brokage.balance;

import com.emce.brokage.balance.entity.Asset;
import com.emce.brokage.balance.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {
    Asset findByAssetName(AssetType assetName);
}
