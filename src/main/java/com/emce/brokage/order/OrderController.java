package com.emce.brokage.order;

import com.emce.brokage.order.dto.OrderRequest;
import com.emce.brokage.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest){
        try {
            return new ResponseEntity<>(orderService.createOrder(orderRequest), HttpStatus.CREATED);

        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }
    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable("orderId") Integer orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
    @PutMapping("/match/{orderId}")
    public ResponseEntity<OrderResponse> matchOrder(@PathVariable("orderId") Integer orderId) {
        return ResponseEntity.ok(orderService.matchOrder(orderId));
    }
}
