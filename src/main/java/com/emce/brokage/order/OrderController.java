package com.emce.brokage.order;

import com.emce.brokage.asset.entity.AssetType;
import com.emce.brokage.order.dto.OrderRequest;
import com.emce.brokage.order.dto.OrderResponse;
import com.emce.brokage.order.entity.OrderSide;
import com.emce.brokage.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest){
        return new ResponseEntity<>(orderService.createOrder(orderRequest), HttpStatus.CREATED);
    }
    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable("orderId") Integer orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
    @PutMapping("/match/{orderId}")
    public ResponseEntity<OrderResponse> matchOrder(@PathVariable("orderId") Integer orderId) {
        return ResponseEntity.ok(orderService.matchOrder(orderId));
    }

    @GetMapping("/list/{customerId}")
    public Page<OrderResponse> getOrders(
            @PathVariable("customerId") Integer customerId,
            @RequestParam(value = "assetName", required = false) AssetType assetName,
            @RequestParam(value = "orderSide", required = false) OrderSide orderSide,
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        return orderService.listOrders(customerId, assetName, orderSide, status, startDate, endDate, pageable);
    }
}
