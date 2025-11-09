package com.finflow.finflow.transaction.dto;

import java.math.BigDecimal;

public record TransactionRequestDto(
        Long fromWalletId, // null for DEPOSIT
        Long toWalletId, // null for WITHDRAW
        BigDecimal amount
) {
}
