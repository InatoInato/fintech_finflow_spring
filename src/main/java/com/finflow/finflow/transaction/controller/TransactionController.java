package com.finflow.finflow.transaction.controller;

import com.finflow.finflow.transaction.dto.TransactionRequestDto;
import com.finflow.finflow.transaction.entity.Transaction;
import com.finflow.finflow.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public Transaction create(
            Authentication authentication,
            @RequestBody TransactionRequestDto request
    ) {
        String email = authentication.getName(); // from JWT
        return transactionService.createTransaction(
                email,
                request.fromWalletId(),
                request.toWalletId(),
                request.amount()
        );
    }
}

