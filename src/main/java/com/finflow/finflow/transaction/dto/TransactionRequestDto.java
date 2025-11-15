package com.finflow.finflow.transaction.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransactionRequestDto(
        @Positive(message = "fromWalletId must be positive")
        Long fromWalletId,

        @Positive(message = "toWalletId must be positive")
        Long toWalletId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount

) {}
