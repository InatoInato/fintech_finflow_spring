package com.finflow.finflow.transaction.service;

import com.finflow.finflow.transaction.entity.Transaction;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

public interface TransactionService {
    public Transaction createTransaction(Authentication authentication,
                                         Long fromWalletId,
                                         Long toWalletId,
                                         BigDecimal amount);

}
