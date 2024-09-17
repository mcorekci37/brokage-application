package com.emce.brokage.order;

import com.emce.brokage.asset.entity.AssetType;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.order.entity.Order;
import com.emce.brokage.order.entity.OrderSide;
import com.emce.brokage.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {
    Page<Order> findByCustomerIdAndAssetNameAndOrderSideAndStatusAndCreatedAtBetween(
            Integer customerId,
            AssetType assetName,
            OrderSide orderSide,
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);
}
