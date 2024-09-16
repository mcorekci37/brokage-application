package com.emce.brokage.balance;

import com.emce.brokage.balance.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {
    Asset findByAssetName(String assetName);
}
