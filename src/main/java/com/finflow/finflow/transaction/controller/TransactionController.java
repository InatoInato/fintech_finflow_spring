package com.finflow.finflow.transaction.controller;

import com.finflow.finflow.transaction.dto.TransactionRequestDto;
import com.finflow.finflow.transaction.entity.Transaction;
import com.finflow.finflow.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService service;

    @PostMapping
    public Transaction createTransaction(@RequestBody TransactionRequestDto dto){
        return service.createTransaction(dto.fromWalletId(), dto.toWalletId(), dto.amount());
    }
}
