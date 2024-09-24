package com.emce.brokage.balance;

import com.emce.brokage.balance.dto.BalanceRequest;
import com.emce.brokage.balance.dto.BalanceResponse;
import com.emce.brokage.balance.entity.TransactionStatus;
import com.emce.brokage.balance.entity.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BalanceControllerTest {

    @InjectMocks
    private BalanceController balanceController;

    @Mock
    private BalanceService balanceService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();  // To serialize and deserialize JSON

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(balanceController).build();
    }

    @Test
    void testDeposit() throws Exception {
        // Given
        Integer customerId = 1;
        BigDecimal amount = BigDecimal.valueOf(1000);
        BalanceRequest request = BalanceRequest.builder()
                .customerId(customerId)
                .amount(amount)
                .transactionType(TransactionType.DEPOSIT)
                .build();
        BalanceResponse expectedResponse = BalanceResponse.builder()
                .customerId(customerId)
                .currentAmount(amount)
                .previousAmount(BigDecimal.ZERO)
                .transactionStatus(TransactionStatus.APPROVED)
                .build();

        when(balanceService.processTransaction(eq(customerId), any(BalanceRequest.class), eq(TransactionType.DEPOSIT)))
                .thenReturn(expectedResponse);

        //when and then
        mockMvc.perform(post("/api/v1/balance/deposit/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.previousAmount").value(0))
                .andExpect(jsonPath("$.currentAmount").value(amount))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.APPROVED.name()));


        verify(balanceService).processTransaction(eq(customerId), any(BalanceRequest.class), eq(TransactionType.DEPOSIT));
    }

    @Test
    void testDeposit_whenCustomerIdNegative() throws Exception {
        // Given
        Integer customerId = -1;
        BigDecimal amount = BigDecimal.valueOf(1000);
        BalanceRequest request = BalanceRequest.builder()
                .customerId(customerId)
                .amount(amount)
                .transactionType(TransactionType.DEPOSIT)
                .build();

        //when and then
        mockMvc.perform(post("/api/v1/balance/deposit/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(balanceService, never()).processTransaction(anyInt(), any(BalanceRequest.class), any(TransactionType.class));
    }
    @Test
    void testDeposit_whenAmountNegative() throws Exception {
        // Given
        Integer customerId = 1;
        BigDecimal amount = BigDecimal.valueOf(-1000);
        BalanceRequest request = BalanceRequest.builder()
                .customerId(customerId)
                .amount(amount)
                .transactionType(TransactionType.DEPOSIT)
                .build();

        //when and then
        mockMvc.perform(post("/api/v1/balance/deposit/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(balanceService, never()).processTransaction(anyInt(), any(BalanceRequest.class), any(TransactionType.class));
    }

    @Test
    void testWithdraw() throws Exception {
        //given
        Integer customerId = 1;
        BigDecimal amount = BigDecimal.valueOf(1000);
        BalanceRequest request = BalanceRequest.builder()
                .customerId(customerId)
                .amount(amount)
                .transactionType(TransactionType.WITHDRAW)
                .build();
        BalanceResponse expectedResponse = BalanceResponse.builder()
                .currentAmount(amount)
                .previousAmount(BigDecimal.ZERO)
                .build();

        when(balanceService.processTransaction(eq(customerId), any(BalanceRequest.class), eq(TransactionType.WITHDRAW)))
                .thenReturn(expectedResponse);

        //when and then
        ResponseEntity<BalanceResponse> response = balanceController.withdraw(customerId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(balanceService).processTransaction(eq(customerId), any(BalanceRequest.class), eq(TransactionType.WITHDRAW));
    }
}
