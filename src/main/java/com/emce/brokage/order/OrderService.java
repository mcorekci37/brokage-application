package com.emce.brokage.order;

import com.emce.brokage.auth.CustomerRepository;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.balance.AssetRepository;
import com.emce.brokage.balance.entity.Asset;
import com.emce.brokage.balance.entity.AssetType;
import com.emce.brokage.entity.Order;
import com.emce.brokage.entity.OrderSide;
import com.emce.brokage.entity.OrderStatus;
import com.emce.brokage.exception.AssetNotEnoughException;
import com.emce.brokage.exception.AssetNotFoundException;
import com.emce.brokage.exception.OrderNotFoundException;
import com.emce.brokage.exception.OrderStatusNotEligibleException;
import com.emce.brokage.exception.UserNotFoundException;
import com.emce.brokage.order.dto.OrderRequest;
import com.emce.brokage.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import static com.emce.brokage.common.Messages.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Customer customer = customerRepository.findCustomerWithAssetsById(request.customerId()).
                orElseThrow(() -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_MSG, request.customerId())));

        Asset tryAsset = getOrCreateAsset(customer, AssetType.TRY);
        Asset orderAsset = getOrCreateAsset(customer, request.assetName());

        Order order = Order.builder()
                .customer(customer)
                .assetName(request.assetName())
                .orderSide(request.orderSide())
                .size(request.size())
                .price(request.price())
                .status(OrderStatus.PENDING)
                .build();


        if (request.orderSide()== OrderSide.SELL){
            processSellOrder(request, tryAsset, orderAsset, order);
        }else if (request.orderSide()== OrderSide.BUY){
            processBuyOrder(request, tryAsset, orderAsset, order);
        }

        assetRepository.save(tryAsset);
        assetRepository.save(orderAsset);
        orderRepository.save(order);

        return OrderResponse.fromEntity(order);
    }

    private void processBuyOrder(OrderRequest request, Asset tryAsset, Asset orderAsset, Order order) {
        if (tryAsset.getUsableSize().compareTo(request.price())<0) {
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
            throw new AssetNotEnoughException(String.format(ASSET_S_HAS_NOT_ENOUGH_SIZE, tryAsset.getAssetName()));
        }
        orderAsset.setSize(orderAsset.getSize().add(request.size()));
        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(request.price()));
    }

    private void processSellOrder(OrderRequest request, Asset tryAsset, Asset orderAsset, Order order) {
        if (orderAsset.getUsableSize().compareTo(request.size())<0) {
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
            throw new AssetNotEnoughException(String.format(ASSET_S_HAS_NOT_ENOUGH_SIZE, request.assetName()));
        }
        orderAsset.setUsableSize(orderAsset.getUsableSize().subtract(request.size()));
        tryAsset.setSize(tryAsset.getSize().add(request.price()));
    }

    private static Asset getOrCreateAsset(Customer customer, AssetType assetName) {
        return customer.getAssets().stream()
                .filter(asset -> asset.getAssetName().equals(assetName)).findFirst()
                .orElseGet(() -> Asset.builder()
                        .assetName(assetName)
                        .size(BigDecimal.ZERO)
                        .usableSize(BigDecimal.ZERO)
                        .customer(customer)
                        .build());
    }

    @Transactional
    public OrderResponse cancelOrder(Integer orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(
                () -> new OrderNotFoundException(String.format(ORDER_NOT_FOUND_MSG, orderId)));
        if (order.getStatus()!=OrderStatus.PENDING){
            throw new OrderStatusNotEligibleException(ONLY_PENDING_ORDERS_CAN_BE_CANCELED_OR_MATCHED_MSG);
        }

        Customer customer = customerRepository.findCustomerWithAssetsById(order.getCustomer().getId()).
                orElseThrow(() -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_MSG, order.getCustomer().getId())));
        Asset orderAsset = customer.getAssets().stream().filter(asset -> asset.getAssetName().equals(order.getAssetName())).findFirst()
                .orElseThrow(() -> new AssetNotFoundException(String.format(ASSET_NOT_FOUND_FOR_ASSET_NAME_S_MSG, order.getAssetName())));
        Asset tryAsset = customer.getAssets().stream().filter(asset -> asset.getAssetName().equals(AssetType.TRY)).findFirst()
                .orElseThrow(() -> new AssetNotFoundException(String.format(ASSET_NOT_FOUND_FOR_ASSET_NAME_S_MSG, AssetType.TRY)));

        if (order.getOrderSide()== OrderSide.SELL){
            tryAsset.setSize(tryAsset.getSize().subtract(order.getPrice()));
            orderAsset.setUsableSize(orderAsset.getUsableSize().add(order.getSize()));
        }else if (order.getOrderSide()== OrderSide.BUY){
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(order.getPrice()));
            orderAsset.setSize(orderAsset.getSize().subtract(order.getSize()));
        }

        order.setStatus(OrderStatus.CANCELED);
        assetRepository.save(orderAsset);
        assetRepository.save(tryAsset);
        orderRepository.save(order);

        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse matchOrder(Integer orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(
                () -> new OrderNotFoundException(String.format(ORDER_NOT_FOUND_MSG, orderId)));
        if (order.getStatus()!=OrderStatus.PENDING){
            throw new OrderStatusNotEligibleException(ONLY_PENDING_ORDERS_CAN_BE_CANCELED_OR_MATCHED_MSG);
        }

        Customer customer = customerRepository.findCustomerWithAssetsById(order.getCustomer().getId()).
                orElseThrow(() -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_MSG, order.getCustomer().getId())));
        Asset orderAsset = customer.getAssets().stream().filter(asset -> asset.getAssetName().equals(order.getAssetName())).findFirst()
                .orElseThrow(() -> new AssetNotFoundException(String.format(ASSET_NOT_FOUND_FOR_ASSET_NAME_S_MSG, order.getAssetName())));
        Asset tryAsset = customer.getAssets().stream().filter(asset -> asset.getAssetName().equals(AssetType.TRY)).findFirst()
                .orElseThrow(() -> new AssetNotFoundException(String.format(ASSET_NOT_FOUND_FOR_ASSET_NAME_S_MSG, AssetType.TRY)));

        if (order.getOrderSide()== OrderSide.SELL){
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(order.getPrice()));
            orderAsset.setSize(orderAsset.getSize().subtract(order.getSize()));
        }else if (order.getOrderSide()== OrderSide.BUY){
            tryAsset.setSize(tryAsset.getSize().subtract(order.getPrice()));
            orderAsset.setUsableSize(orderAsset.getUsableSize().add(order.getSize()));
        }

        assetRepository.save(orderAsset);
        assetRepository.save(tryAsset);
        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);

        return OrderResponse.fromEntity(order);
    }
}
