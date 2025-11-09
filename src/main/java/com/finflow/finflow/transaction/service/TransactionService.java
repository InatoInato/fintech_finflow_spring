package com.finflow.finflow.transaction.service;

import com.finflow.finflow.transaction.entity.Transaction;

import java.math.BigDecimal;

public interface TransactionService {
    public Transaction createTransaction(Long fromWalletId, Long toWalletId, BigDecimal amount);

}
