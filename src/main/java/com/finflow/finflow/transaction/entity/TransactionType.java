package com.finflow.finflow.transaction.entity;

import com.finflow.finflow.wallet.entity.Wallet;

public enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER;

    public static TransactionType resolve(Wallet from, Wallet to) {
        if (from != null && to != null) return TRANSFER;
        if (from == null) return DEPOSIT;
        return WITHDRAW;
    }
}
