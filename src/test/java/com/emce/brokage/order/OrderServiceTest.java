package com.emce.brokage.order;

import com.emce.brokage.asset.AssetRepository;
import com.emce.brokage.asset.entity.Asset;
import com.emce.brokage.asset.entity.AssetType;
import com.emce.brokage.auth.CustomerRepository;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.auth.entity.Role;
import com.emce.brokage.exception.AssetNotEnoughException;
import com.emce.brokage.exception.OrderNotFoundException;
import com.emce.brokage.order.dto.OrderRequest;
import com.emce.brokage.order.dto.OrderResponse;
import com.emce.brokage.order.entity.Order;
import com.emce.brokage.order.entity.OrderSide;
import com.emce.brokage.order.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import static com.emce.brokage.common.Messages.ASSET_S_HAS_NOT_ENOUGH_SIZE_MSG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private CustomerRepository customerRepository;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    private static Customer createSimpleCustomer() {
        return Customer.builder()
                .id(1)
                .name("testName")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .assets(new HashSet<>())
                .build();
    }

    @Test
    void testCreateOrder_Success() {
        // Given
        Customer customer = createSimpleCustomer();
        customer.getAssets().add(createSimpleAsset(AssetType.USD, BigDecimal.ZERO, 1));
        BigDecimal price = BigDecimal.valueOf(100);
        customer.getAssets().add(createSimpleAsset(AssetType.TRY, price, 2));

        BigDecimal size = BigDecimal.TEN;
        OrderRequest request = new OrderRequest(customer.getId(), AssetType.USD, OrderSide.BUY, size, price);
        when(customerRepository.findCustomerWithAssetsById(request.customerId()))
                .thenReturn(Optional.of(customer));

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(size, response.size());
        assertEquals(price, response.price());
        verify(orderRepository).save(any(Order.class));
        verify(assetRepository, times(2)).save(any(Asset.class)); // For tryAsset and orderAsset
    }

    @Test
    void testCreateOrder_AssetNotEnough() {
        // Given
        Customer customer = createSimpleCustomer();
        customer.getAssets().add(createSimpleAsset(AssetType.USD, BigDecimal.ZERO, 1));
        BigDecimal price = BigDecimal.valueOf(100);
        customer.getAssets().add(createSimpleAsset(AssetType.TRY, price.subtract(BigDecimal.ONE), 2));

        BigDecimal size = BigDecimal.TEN;
        OrderRequest request = new OrderRequest(customer.getId(), AssetType.USD, OrderSide.BUY, size, price);

        when(customerRepository.findCustomerWithAssetsById(request.customerId()))
                .thenReturn(Optional.of(customer));

        // When
        Exception exception = assertThrows(AssetNotEnoughException.class, () ->
                orderService.createOrder(request));

        // Then
        assertEquals(String.format(ASSET_S_HAS_NOT_ENOUGH_SIZE_MSG, AssetType.TRY), exception.getMessage());
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void testCancelOrder_Success() {
        // Given
        Customer customer = createSimpleCustomer();
        Asset usdAsset = createSimpleAsset(AssetType.USD, BigDecimal.ZERO, 1);
        BigDecimal size = BigDecimal.TEN;
        usdAsset.setSize(usdAsset.getSize().add(size));
        BigDecimal price = BigDecimal.valueOf(100);
        Asset tryAsset = createSimpleAsset(AssetType.TRY, price, 2);
        tryAsset.setUsableSize(BigDecimal.ZERO);
        customer.getAssets().add(usdAsset);
        customer.getAssets().add(tryAsset);


        Order order = Order.builder()
                .id(1)
                .size(size)
                .assetName(AssetType.USD)
                .orderSide(OrderSide.BUY)
                .price(price)
                .customer(customer)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));
        when(customerRepository.findCustomerWithAssetsById(order.getCustomer().getId()))
                .thenReturn(Optional.of(customer));

        // When
        OrderResponse response = orderService.cancelOrder(1);

        // Then
        assertEquals(OrderStatus.CANCELED, response.status());
        assertEquals(size, response.size());
        assertEquals(price, response.price());
        verify(orderRepository).save(order);
    }

    @Test
    void testCancelOrder_OrderNotFound() {
        // Given
        when(orderRepository.findById(1))
                .thenReturn(Optional.empty());

        // When / Then
        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(1));
    }

    @Test
    void testMatchOrder_Success() {
        // Given
        Customer customer = createSimpleCustomer();
        Asset usdAsset = createSimpleAsset(AssetType.USD, BigDecimal.ZERO, 1);
        BigDecimal size = BigDecimal.TEN;
        usdAsset.setSize(usdAsset.getSize().add(size));
        BigDecimal price = BigDecimal.valueOf(100);
        Asset tryAsset = createSimpleAsset(AssetType.TRY, price, 2);
        tryAsset.setUsableSize(BigDecimal.ZERO);
        customer.getAssets().add(usdAsset);
        customer.getAssets().add(tryAsset);


        Order order = Order.builder()
                .id(1)
                .size(size)
                .assetName(AssetType.USD)
                .orderSide(OrderSide.BUY)
                .price(price)
                .customer(customer)
                .status(OrderStatus.PENDING)
                .build();
        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));
        when(customerRepository.findCustomerWithAssetsById(order.getCustomer().getId()))
                .thenReturn(Optional.of(customer));

        // When
        OrderResponse response = orderService.matchOrder(1);

        // Then
        assertEquals(OrderStatus.MATCHED, response.status());
        assertEquals(size, response.size());
        assertEquals(price, response.price());
        verify(orderRepository).save(order);
    }

    private static Asset createSimpleAsset(AssetType assetName, BigDecimal size, int id) {
        return Asset.builder()
                .id(id)
                .usableSize(size)
                .size(size)
                .assetName(assetName)
                .build();
    }

    private static Order createSimpleOrder() {
        Customer customer = createSimpleCustomer();
        return Order.builder()
                .id(1)
                .size(BigDecimal.TEN)
                .assetName(AssetType.USD)
                .orderSide(OrderSide.BUY)
                .customer(customer)
                .build();
    }

    @Test
    void testMatchOrder_OrderNotFound() {
        // Given
        when(orderRepository.findById(1))
                .thenReturn(Optional.empty());

        // When / Then
        assertThrows(OrderNotFoundException.class, () -> orderService.matchOrder(1));
    }

    @Test
    void listOrders_shouldReturnPagedOrders_whenValidInputsProvided() {
        // Given
        Integer customerId = 1;
        AssetType assetName = AssetType.TRY;
        OrderSide orderSide = OrderSide.BUY;
        OrderStatus status = OrderStatus.PENDING;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        Order order = createSimpleOrder();

        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));

        // Mock the repository call
        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(orderPage);

        // When
        Page<OrderResponse> result = orderService
                .listOrders(customerId, assetName, orderSide, status, startDate, endDate, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void listOrders_shouldReturnEmptyPage_whenNoOrdersMatch() {
        // Given
        Integer customerId = 1;
        AssetType assetName = AssetType.TRY;
        OrderSide orderSide = OrderSide.BUY;
        OrderStatus status = OrderStatus.PENDING;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> emptyPage = Page.empty();

        // Mock the repository call
        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        // When
        Page<OrderResponse> result = orderService
                .listOrders(customerId, assetName, orderSide, status, startDate, endDate, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void listOrders_shouldReturnOrders_whenOnlyCustomerIdProvided() {
        // Given
        Integer customerId = 1;
        Pageable pageable = PageRequest.of(0, 10);

        Order order = new Order();
        order.setCustomer(createSimpleCustomer());
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));

        // Mock the repository call
        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(orderPage);

        // When
        Page<OrderResponse> result = orderService
                .listOrders(customerId, null, null, null, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1))
                .findAll(any(Specification.class), eq(pageable));
    }}
