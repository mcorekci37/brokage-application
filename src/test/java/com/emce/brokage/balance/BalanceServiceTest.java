package com.emce.brokage.balance;

import com.emce.brokage.asset.AssetRepository;
import com.emce.brokage.asset.entity.Asset;
import com.emce.brokage.asset.entity.AssetType;
import com.emce.brokage.auth.CustomerRepository;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.balance.dto.BalanceRequest;
import com.emce.brokage.balance.dto.BalanceResponse;
import com.emce.brokage.balance.entity.AccountTransaction;
import com.emce.brokage.balance.entity.TransactionStatus;
import com.emce.brokage.balance.entity.TransactionType;
import com.emce.brokage.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Optional;
import static com.emce.brokage.common.Messages.CUSTOMER_ID_IN_PATH_AND_BODY_NOT_MATCH_MSG;
import static com.emce.brokage.common.Messages.USER_ID_NOT_FOUND_MSG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BalanceServiceTest {

    @InjectMocks
    private BalanceService balanceService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AssetRepository assetRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customer = Customer.builder()
                .id(1)
                .assets(new HashSet<>())
                .build();
    }

    @Test
    void testProcessTransaction_Deposit_Success() {
        // Given
        Integer customerId = 1;
        BalanceRequest request = new BalanceRequest(customerId, BigDecimal.valueOf(100), TransactionType.DEPOSIT);
        Asset tryAsset = createSimpleAsset(BigDecimal.ZERO);
        customer.getAssets().add(tryAsset);

        when(customerRepository.findCustomerWithAssetsById(customerId))
                .thenReturn(Optional.of(customer));

        // When
        BalanceResponse response = balanceService.processTransaction(customerId, request, TransactionType.DEPOSIT);

        // Then
        assertEquals(customerId, response.customerId());
        assertEquals(BigDecimal.ZERO, response.previousAmount());
        assertEquals(BigDecimal.valueOf(100), response.currentAmount());
        assertEquals(TransactionStatus.APPROVED, response.transactionStatus());

        verify(transactionRepository).save(any(AccountTransaction.class));
        verify(assetRepository).save(any(Asset.class));
    }

    private Asset createSimpleAsset(BigDecimal size) {
        return Asset.builder()
                .assetName(AssetType.TRY)
                .usableSize(size)
                .size(size)
                .customer(customer)
                .build();
    }

    @Test
    void testProcessTransaction_Withdraw_Success() {
        // Given
        Integer customerId = 1;
        BalanceRequest request = new BalanceRequest(customerId, BigDecimal.valueOf(50), TransactionType.WITHDRAW);
        Asset tryAsset = createSimpleAsset(BigDecimal.valueOf(100));
        customer.getAssets().add(tryAsset);

        when(customerRepository.findCustomerWithAssetsById(customerId))
                .thenReturn(Optional.of(customer));

        // When
        BalanceResponse response = balanceService
                .processTransaction(customerId, request, TransactionType.WITHDRAW);

        // Then
        assertEquals(customerId, response.customerId());
        assertEquals(BigDecimal.valueOf(100), response.previousAmount());
        assertEquals(BigDecimal.valueOf(50), response.currentAmount());
        assertEquals(TransactionStatus.APPROVED, response.transactionStatus());

        verify(transactionRepository).save(any(AccountTransaction.class));
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    void testProcessTransaction_UserNotFound() {
        // Given
        Integer customerId = 1;
        BalanceRequest request = new BalanceRequest(customerId, BigDecimal.valueOf(100), TransactionType.DEPOSIT);

        when(customerRepository.findCustomerWithAssetsById(customerId)).thenReturn(Optional.empty());

        // Then
        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> {
            balanceService.processTransaction(customerId, request, TransactionType.DEPOSIT);
        });

        assertEquals(String.format(USER_ID_NOT_FOUND_MSG, customerId), thrown.getMessage());
    }

    @Test
    void testProcessTransaction_InvalidCustomerId() {
        // Given
        Integer customerId = 1;
        BalanceRequest request = new BalanceRequest(2, BigDecimal.valueOf(100), TransactionType.DEPOSIT);

        // Then
        InvalidParameterException thrown = assertThrows(InvalidParameterException.class, () -> {
            balanceService.processTransaction(customerId, request, TransactionType.DEPOSIT);
        });

        assertEquals(CUSTOMER_ID_IN_PATH_AND_BODY_NOT_MATCH_MSG, thrown.getMessage());
    }

    @Test
    void testProcessTransaction_InvalidTransactionType() {
        // Given
        Integer customerId = 1;
        BalanceRequest request = new BalanceRequest(customerId, BigDecimal.valueOf(100), TransactionType.WITHDRAW);
        Asset tryAsset = createSimpleAsset(BigDecimal.ZERO);
        customer.getAssets().add(tryAsset);

        when(customerRepository.findCustomerWithAssetsById(customerId))
                .thenReturn(Optional.of(customer));

        // Then
        InvalidParameterException thrown = assertThrows(InvalidParameterException.class, () -> {
            balanceService.processTransaction(customerId, request, TransactionType.DEPOSIT);
        });

        assertEquals("You can only DEPOSIT from this endpoint", thrown.getMessage());
    }
}
