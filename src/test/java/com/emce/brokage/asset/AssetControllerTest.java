package com.emce.brokage.asset;

import com.emce.brokage.asset.dto.AssetDto;
import com.emce.brokage.asset.entity.AssetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class AssetControllerTest {

    @InjectMocks
    private AssetController assetController;

    @Mock
    private AssetService assetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAssetsForCustomer() {
        // Given
        Integer customerId = 1;
        AssetType assetName = AssetType.TRY; // Replace with an actual AssetType
        Pageable pageable = PageRequest.of(0, 10);
        AssetDto assetDto = AssetDto.builder()
                .id(1)
                .assetName(assetName)
                .customerId(customerId)
                .build();
        List<AssetDto> assetList = List.of(assetDto);
        Page<AssetDto> assetPage = new PageImpl<>(assetList, pageable, assetList.size());

        // Mocking the service layer
        when(assetService.getAssetsForCustomer(anyInt(), ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(assetPage);

        // When
        ResponseEntity<Page<AssetDto>> response = assetController
                .getAssetsForCustomer(customerId, assetName, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(assetPage, response.getBody());
    }

    @Test
    void testGetAssetsForCustomer_WithInvalidCustomerId() {
        // Given
        Integer customerId = -1; // Invalid customer ID
        AssetType assetName = AssetType.USD; // No asset type filter
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        try {
            assetController.getAssetsForCustomer(customerId, assetName, pageable);
        } catch (Exception e) {
            assertEquals("Customer ID must be a positive number", e.getMessage());
        }
    }

}
