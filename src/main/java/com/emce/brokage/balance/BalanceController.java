package com.emce.brokage.balance;

import com.emce.brokage.balance.dto.BalanceRequest;
import com.emce.brokage.balance.dto.BalanceResponse;
import com.emce.brokage.balance.entity.TransactionType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/balance")
public class BalanceController {

    private final BalanceService balanceService;

    @PostMapping("/deposit/{customerId}")
    public ResponseEntity<BalanceResponse> deposit(@PathVariable("customerId") Integer customerId, @Valid @RequestBody BalanceRequest request){
        return ResponseEntity.ok(balanceService.processTransaction(customerId, request, TransactionType.DEPOSIT));
    }
    @PostMapping("/withdraw/{customerId}")
    public ResponseEntity<BalanceResponse> withdraw(@PathVariable("customerId") Integer customerId, @Valid @RequestBody BalanceRequest request){
        return ResponseEntity.ok(balanceService.processTransaction(customerId, request, TransactionType.WITHDRAW));
    }

}
