package com.emce.brokage.balance;

import com.emce.brokage.auth.CustomerRepository;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.balance.dto.BalanceRequest;
import com.emce.brokage.balance.dto.BalanceResponse;
import com.emce.brokage.balance.entity.AccountTransaction;
import com.emce.brokage.balance.entity.Asset;
import com.emce.brokage.balance.entity.TransactionStatus;
import com.emce.brokage.balance.entity.TransactionType;
import com.emce.brokage.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import static com.emce.brokage.common.Messages.*;

@Service
@RequiredArgsConstructor
public class BalanceService {

    public static final String TRY = "TRY";
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;

    public BalanceResponse processTransaction(BalanceRequest request, TransactionType transactionType) {
        Customer customer = customerRepository.findCustomerWithAssetsById(request.customerId()).
                orElseThrow(() -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_MSG, request.customerId())));

        Optional<Asset> tryAssetOpt = customer.getAssets().stream().filter(asset -> asset.getAssetName().equals(TRY)).findFirst();
        Asset tryAsset = tryAssetOpt.orElseGet(() -> Asset.builder()
                .assetName(TRY)
                .size(BigDecimal.ZERO)
                .usableSize(BigDecimal.ZERO)
                .customer(customer)
                .build());

        BigDecimal previousAmount = tryAsset.getUsableSize();
        BigDecimal requestedAmount = request.amount();
        TransactionStatus status = getTransactionStatus(tryAsset, requestedAmount, request.transactionType());

        AccountTransaction accountTransaction = AccountTransaction.builder()
                .customer(customer)
                .amount(requestedAmount)
                .orderSide(transactionType)
                .status(status)
                .build();
        transactionRepository.save(accountTransaction);
        assetRepository.save(tryAsset);

        return BalanceResponse.builder()
                .customerId(customer.getId())
                .previousAmount(previousAmount)
                .currentAmount(tryAsset.getUsableSize())
                .transactionStatus(status)
                .build();
    }

    private TransactionStatus getTransactionStatus(Asset tryAsset, BigDecimal requestedAmount, TransactionType transactionType) {
        TransactionStatus status = TransactionStatus.APPROVED;
        BigDecimal previousUsableSize = tryAsset.getUsableSize();
        BigDecimal previousSize = tryAsset.getSize();
        if (transactionType == TransactionType.DEPOSIT){
            tryAsset.setUsableSize(previousUsableSize.add(requestedAmount));
            tryAsset.setSize(previousSize.add(requestedAmount));
        }else if (transactionType == TransactionType.WITHDRAW){
            if (previousUsableSize.compareTo(requestedAmount)<0){
                status = TransactionStatus.CANCELED;
            }else {
                tryAsset.setUsableSize(previousUsableSize.subtract(requestedAmount));
                tryAsset.setSize(previousSize.subtract(requestedAmount));
            }
        }
        return status;
    }
}
