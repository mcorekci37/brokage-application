package com.emce.brokage.balance;

import com.emce.brokage.auth.CustomerRepository;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.balance.dto.BalanceRequest;
import com.emce.brokage.balance.dto.BalanceResponse;
import com.emce.brokage.balance.entity.AccountTransaction;
import com.emce.brokage.entity.TransactionStatus;
import com.emce.brokage.entity.TransactionType;
import com.emce.brokage.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import static com.emce.brokage.common.Messages.*;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    public BalanceResponse processTransaction(BalanceRequest request, TransactionType transactionType) {
        Customer customer = customerRepository.findById(request.customerId()).
                orElseThrow(() -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_MSG, request.customerId())));

        Double previousAmount = customer.getAccountBalance();
        Double requestedAmount = request.amount();
        TransactionStatus status = getTransactionStatus(customer, requestedAmount, request.transactionType());

        AccountTransaction accountTransaction = AccountTransaction.builder()
                .customer(customer)
                .amount(requestedAmount)
                .orderSide(transactionType)
                .status(status)
                .build();
        transactionRepository.save(accountTransaction);

        return BalanceResponse.builder()
                .customerId(customer.getId())
                .previousAmount(previousAmount)
                .currentAmount(customer.getAccountBalance())
                .transactionStatus(status)
                .build();
    }

    private static TransactionStatus getTransactionStatus(Customer customer, Double requestedAmount, TransactionType transactionType) {
        TransactionStatus status = TransactionStatus.APPROVED;
        Double previousAmount = customer.getAccountBalance();
        if (transactionType == TransactionType.DEPOSIT){
            customer.setAccountBalance(previousAmount + requestedAmount);
        }else if (transactionType == TransactionType.WITHDRAW){
            if (previousAmount < requestedAmount){
                status = TransactionStatus.CANCELED;
            }else {
                customer.setAccountBalance(previousAmount - requestedAmount);
            }
        }
        return status;
    }
}
