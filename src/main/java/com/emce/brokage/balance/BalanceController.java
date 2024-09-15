package com.emce.brokage.balance;

import com.emce.brokage.balance.dto.BalanceRequest;
import com.emce.brokage.balance.dto.BalanceResponse;
import com.emce.brokage.entity.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidParameterException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/balance")
public class BalanceController {

    private final BalanceService balanceService;

    @PostMapping("/deposit/{customerId}")
    public ResponseEntity<BalanceResponse> deposit(@PathVariable("customerId") Integer customerId, @RequestBody BalanceRequest request){
        if (!customerId.equals(request.customerId())){
            throw new InvalidParameterException("customerId in path and body doesn't match");
        }
        if (request.transactionType()!= TransactionType.DEPOSIT){
            throw new InvalidParameterException("You can only deposit from this endpoint");
        }
        return ResponseEntity.ok(balanceService.processTransaction(request, TransactionType.DEPOSIT));
    }
    @PostMapping("/withdraw/{customerId}")
    public ResponseEntity<BalanceResponse> withdraw(@PathVariable("customerId") Integer customerId, @RequestBody BalanceRequest request){
        if (!customerId.equals(request.customerId())){
            throw new InvalidParameterException("customerId in path and body doesn't match");
        }
        if (request.transactionType()!= TransactionType.WITHDRAW){
            throw new InvalidParameterException("You can only deposit from this endpoint");
        }
        return ResponseEntity.ok(balanceService.processTransaction(request, TransactionType.WITHDRAW));
    }

}
