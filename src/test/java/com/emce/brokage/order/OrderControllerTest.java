package com.emce.brokage.order;

import com.emce.brokage.asset.entity.AssetType;
import com.emce.brokage.order.dto.OrderRequest;
import com.emce.brokage.order.dto.OrderResponse;
import com.emce.brokage.order.entity.OrderSide;
import com.emce.brokage.order.entity.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    private ObjectMapper objectMapper = new ObjectMapper();  // To serialize and deserialize JSON
    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void testCreateOrder() throws Exception {
        //given
        int customerId = 1;
        OrderRequest orderRequest = new OrderRequest(customerId, AssetType.USD, OrderSide.BUY, BigDecimal.TEN, BigDecimal.valueOf(100));
        OrderResponse orderResponse = new OrderResponse(customerId, AssetType.USD, OrderSide.BUY, BigDecimal.TEN, BigDecimal.valueOf(100),
                OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());

        when(orderService.createOrder(any(OrderRequest.class)))
                .thenReturn(orderResponse);

        //when and then
        mockMvc.perform(post("/api/v1/order/create")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(orderResponse.customerId()));

        verify(orderService).createOrder(any(OrderRequest.class));
    }

    @Test
    void testCancelOrder() throws Exception {
        //given
        Integer orderId = 1;
        Integer customerId = 1;
        OrderResponse orderResponse = new OrderResponse(customerId, AssetType.USD, OrderSide.BUY, BigDecimal.TEN, BigDecimal.valueOf(100),
                OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());

        when(orderService.cancelOrder(orderId)).thenReturn(orderResponse);

        //when and then
        mockMvc.perform(delete("/api/v1/order/cancel/{orderId}", orderId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(orderResponse.customerId()));

        verify(orderService).cancelOrder(orderId);
    }

    @Test
    void testMatchOrder() throws Exception {
        //given
        Integer orderId = 1;
        Integer customerId = 1;
        OrderResponse orderResponse = new OrderResponse(customerId, AssetType.USD, OrderSide.BUY, BigDecimal.TEN, BigDecimal.valueOf(100),
                OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());

        when(orderService.matchOrder(orderId)).thenReturn(orderResponse);

        //when and then
        mockMvc.perform(put("/api/v1/order/match/{orderId}", orderId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(orderResponse.customerId()));

        verify(orderService).matchOrder(orderId);
    }
}
