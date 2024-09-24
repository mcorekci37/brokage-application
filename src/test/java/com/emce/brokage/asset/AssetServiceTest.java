package com.emce.brokage.asset;

import com.emce.brokage.asset.dto.AssetDto;
import com.emce.brokage.asset.entity.Asset;
import com.emce.brokage.asset.entity.AssetType;
import com.emce.brokage.auth.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAssetsForCustomer_shouldReturnAssets_whenCustomerIdMatches() {
        // Given
        Integer customerId = 1;
        AssetType assetType = AssetType.TRY;
        Asset asset = createBasicAsset(customerId, assetType); // Create an example entity
        Pageable pageable = PageRequest.of(0, 10);

        when(assetRepository.findByCustomerIdAndAssetType(customerId, assetType, pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(asset)));

        //when
        Page<AssetDto> result = assetService.getAssetsForCustomer(customerId, assetType, pageable);

        //then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertInstanceOf(AssetDto.class, result.getContent().get(0));
        assertEquals(asset.getCustomer().getId(), result.getContent().get(0).customerId());
        assertEquals(asset.getAssetName(), result.getContent().get(0).assetName());

        verify(assetRepository, times(1))
                .findByCustomerIdAndAssetType(customerId, assetType, pageable);
    }
    @Test
    void getAssetsForCustomer_shouldReturnEmptyPage_whenNoAssetsAreFound() {
        // Given
        Integer customerId = 1;
        AssetType assetType = AssetType.TRY;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Asset> emptyPage = Page.empty();

        when(assetRepository.findByCustomerIdAndAssetType(customerId, assetType, pageable)).thenReturn(emptyPage);

        // when
        Page<AssetDto> result = assetService.getAssetsForCustomer(customerId, assetType, pageable);

        // than
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(assetRepository, times(1))
                .findByCustomerIdAndAssetType(customerId, assetType, pageable);
    }


    private Asset createBasicAsset(Integer customerId, AssetType assetType) {
        Customer customer = new Customer();
        customer.setId(customerId);
        BigDecimal size = new BigDecimal(100);
        return Asset.builder()
                .id(1)
                .assetName(assetType)
                .size(size)
                .usableSize(size)
                .customer(customer)
                .build();
    }

}
